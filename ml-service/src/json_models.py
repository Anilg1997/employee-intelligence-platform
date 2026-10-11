"""Versioned, data-only sklearn exports and NumPy inference.

Only the explicitly supported fitted transformations are exported. Loading and
prediction do not import sklearn or instantiate objects named by artifact data.
"""
import json
from pathlib import Path

import numpy as np

SCHEMA_VERSION = 1


def _require(condition, message):
    if not condition:
        raise ValueError(message)


def _keys(value, keys):
    _require(isinstance(value, dict) and set(value) == set(keys), "Invalid artifact structure")


def _finite_json(value):
    if isinstance(value, dict):
        _require(all(isinstance(k, str) for k in value), "JSON keys must be strings")
        for item in value.values():
            _finite_json(item)
    elif isinstance(value, list):
        for item in value:
            _finite_json(item)
    else:
        _require(value is None or type(value) in (str, bool, int, float), "Not a JSON value")
        if type(value) in (int, float):
            try:
                finite = np.isfinite(float(value))
            except (OverflowError, ValueError):
                finite = False
            _require(finite, "Nonfinite JSON number")


def _numbers(value, shape, integer=False):
    _require(isinstance(value, list), "Expected numeric array")
    array = np.asarray(value)
    _require(array.shape == shape and array.dtype.kind in ("iu" if integer else "iuf"),
             "Invalid numeric array shape or type")
    _require(np.isfinite(array).all(), "Nonfinite numeric array")
    if integer and array.dtype.kind == "u":
        _require((array <= np.iinfo(np.int64).max).all(), "Integer array exceeds int64 range")
    return array.astype(np.int64 if integer else np.float64)


def _labels(value):
    _require(isinstance(value, list) and len(value) > 0, "Missing labels")
    _require(all(type(v) is str for v in value) or
             all(type(v) in (int, float, bool) for v in value), "Unsupported label type")
    _require(len(set(value)) == len(value), "Duplicate labels")


def _validate(artifact):
    _finite_json(artifact)
    _keys(artifact, ("schema_version", "metadata", "preprocessing", "estimator"))
    _require(type(artifact["schema_version"]) is int and artifact["schema_version"] == SCHEMA_VERSION,
             "Unsupported schema version")
    _require(isinstance(artifact["metadata"], dict), "Metadata must be an object")
    prep = artifact["preprocessing"]
    _keys(prep, ("features", "transformers", "n_outputs"))
    features = prep["features"]
    _require(isinstance(features, list) and features and all(type(x) is str for x in features)
             and len(set(features)) == len(features), "Invalid input features")
    _require(isinstance(prep["transformers"], list), "Invalid transformers")
    width = 0
    for block in prep["transformers"]:
        _require(isinstance(block, dict), "Invalid transformer")
        kind = block.get("kind")
        extras = {"passthrough": (), "standard_scaler": ("mean", "scale"),
                  "one_hot": ("categories",)}
        _require(type(kind) is str and kind in extras, "Unsupported transformer")
        _keys(block, ("kind", "columns") + extras[kind])
        columns = block["columns"]
        _require(isinstance(columns, list) and columns and all(type(x) is str and x in features for x in columns)
                 and len(set(columns)) == len(columns), "Invalid transformer columns")
        if kind == "standard_scaler":
            _numbers(block["mean"], (len(columns),))
            _require((_numbers(block["scale"], (len(columns),)) > 0).all(), "Invalid scaler scale")
        if kind == "one_hot":
            cats = block["categories"]
            _require(isinstance(cats, list) and len(cats) == len(columns), "Invalid encoder categories")
            for labels in cats:
                _labels(labels)
                width += len(labels)
        else:
            width += len(columns)
    _require(type(prep["n_outputs"]) is int and width == prep["n_outputs"] and width > 0,
             "Invalid transformed feature count")
    est = artifact["estimator"]
    _require(isinstance(est, dict), "Invalid estimator")
    kind = est.get("kind")
    _require(kind in ("logistic_regression", "random_forest_classifier", "random_forest_regressor"),
             "Unsupported estimator")
    classification = kind != "random_forest_regressor"
    if classification:
        _labels(est.get("classes"))
    if kind == "logistic_regression":
        _keys(est, ("kind", "classes", "coef", "intercept"))
        _require(len(est["classes"]) == 2, "Only binary logistic regression is supported")
        _numbers(est["coef"], (width,))
        _numbers(est["intercept"], (1,))
        return
    _keys(est, ("kind", "trees", "classes") if classification else ("kind", "trees"))
    _require(isinstance(est["trees"], list) and est["trees"], "Forest must contain trees")
    for tree in est["trees"]:
        _keys(tree, ("left", "right", "feature", "threshold", "value"))
        _require(isinstance(tree["left"], list) and tree["left"], "Empty tree")
        n = len(tree["left"])
        left = _numbers(tree["left"], (n,), True)
        right = _numbers(tree["right"], (n,), True)
        feature = _numbers(tree["feature"], (n,), True)
        _numbers(tree["threshold"], (n,))
        values = _numbers(tree["value"], (n, len(est["classes"]) if classification else 1))
        if classification:
            with np.errstate(over="ignore"):
                totals = values.sum(axis=1)
            _require((values >= 0).all() and (totals > 0).all() and np.isfinite(totals).all(),
                     "Invalid class counts")
        # A rooted tree must visit each node exactly once: rejects cycles,
        # multiple parents, disconnected nodes, and out-of-range child indices.
        pending, seen = [0], set()
        while pending:
            node = pending.pop()
            _require(0 <= node < n and node not in seen, "Invalid tree topology")
            seen.add(node)
            if left[node] == -1 or right[node] == -1:
                _require(left[node] == right[node] == -1 and feature[node] == -2,
                         "Invalid leaf")
            else:
                _require(0 <= feature[node] < width, "Invalid tree feature")
                pending.extend((int(left[node]), int(right[node])))
        _require(len(seen) == n, "Unreachable tree nodes")


def _no_duplicates(pairs):
    result = {}
    for key, value in pairs:
        _require(key not in result, "Duplicate JSON key")
        result[key] = value
    return result


def load_artifact(path):
    """Read and validate one JSON document; return model and its metadata."""
    with Path(path).open(encoding="utf-8") as stream:
        artifact = json.load(stream, object_pairs_hook=_no_duplicates,
                             parse_constant=lambda value: (_ for _ in ()).throw(ValueError("Nonfinite JSON number")))
    return {"model": JsonPredictor(artifact), "metadata": artifact["metadata"]}


class JsonPredictor:
    """Predict from a validated artifact using only fixed NumPy operations."""

    def __init__(self, artifact):
        _validate(artifact)
        # Detach internal state from callers' mutable dictionaries.
        artifact = json.loads(json.dumps(artifact, allow_nan=False))
        self._prep = artifact["preprocessing"]
        self._est = artifact["estimator"]
        if "classes" in self._est:
            self.classes_ = np.asarray(self._est["classes"])
        self._trees = [{key: np.asarray(value, dtype=np.int64 if key in ("left", "right", "feature") else np.float64)
                        for key, value in tree.items()} for tree in self._est.get("trees", [])]

    def _transform(self, frame):
        _require(hasattr(frame, "columns") and hasattr(frame, "loc") and frame.columns.is_unique,
                 "Expected a DataFrame with unique columns")
        _require(all(name in frame.columns for block in self._prep["transformers"] for name in block["columns"]),
                 "Missing input columns")
        blocks = []
        for block in self._prep["transformers"]:
            values = frame.loc[:, block["columns"]].to_numpy()
            if block["kind"] == "one_hot":
                encoded = []
                for index, categories in enumerate(block["categories"]):
                    column = values[:, index]
                    _require(all(isinstance(v, (str, int, float, bool, np.number)) and
                                 (isinstance(v, str) or np.isfinite(v)) for v in column),
                             "Invalid categorical input")
                    encoded.append(np.column_stack([column == category for category in categories]).astype(np.float64))
                blocks.append(np.hstack(encoded))
            else:
                # StandardScaler retains float16/float32 input precision and
                # rounds after each in-place operation, as sklearn does.
                values = values.astype(values.dtype if values.dtype.kind == "f" and values.dtype.itemsize <= 8 else np.float64)
                _require(np.isfinite(values).all(), "Nonfinite numeric input")
                if block["kind"] == "standard_scaler":
                    values -= np.asarray(block["mean"], dtype=values.dtype)
                    values /= np.asarray(block["scale"], dtype=values.dtype)
                blocks.append(values)
        transformed = np.hstack(blocks)
        _require(np.isfinite(transformed).all(), "Nonfinite transformed input")
        return transformed

    def _forest(self, values):
        # sklearn's tree routines cast transformed inputs to float32 before
        # comparing them with double-precision thresholds.
        with np.errstate(over="ignore"):
            values = values.astype(np.float32)
        _require(np.isfinite(values).all(), "Tree input exceeds float32 range")
        classifier = "classes" in self._est
        result = np.zeros((len(values), len(self.classes_) if classifier else 1))
        for tree in self._trees:
            nodes = np.zeros(len(values), dtype=np.int64)
            active = np.arange(len(values))
            while len(active):
                current = nodes[active]
                active = active[tree["left"][current] != -1]
                current = nodes[active]
                nodes[active] = np.where(values[active, tree["feature"][current]] <= tree["threshold"][current],
                                         tree["left"][current], tree["right"][current])
            output = tree["value"][nodes]
            if classifier:
                output = output / output.sum(axis=1, keepdims=True)
            result += output
        _require(np.isfinite(result).all(), "Nonfinite forest output")
        return result / len(self._trees)

    def predict_proba(self, frame):
        _require("classes" in self._est, "Regressor has no class probabilities")
        values = self._transform(frame)
        if self._est["kind"] == "logistic_regression":
            scores = values @ np.asarray(self._est["coef"]) + self._est["intercept"][0]
            _require(np.isfinite(scores).all(), "Nonfinite logistic score")
            probabilities = np.empty_like(scores)
            positive = scores >= 0
            probabilities[positive] = 1 / (1 + np.exp(-scores[positive]))
            exp_scores = np.exp(scores[~positive])
            probabilities[~positive] = exp_scores / (1 + exp_scores)
            return np.column_stack((1 - probabilities, probabilities))
        return self._forest(values)

    def predict(self, frame):
        if self._est["kind"] == "logistic_regression":
            # sklearn uses decision_function > 0, even for scores so tiny
            # sigmoid rounds them to exactly 0.5.
            scores = self._transform(frame) @ np.asarray(self._est["coef"]) + self._est["intercept"][0]
            _require(np.isfinite(scores).all(), "Nonfinite logistic score")
            return self.classes_[(scores > 0).astype(int)]
        if "classes" in self._est:
            return self.classes_[np.argmax(self.predict_proba(frame), axis=1)]
        return self._forest(self._transform(frame))[:, 0]


def export_pipeline(pipeline, metadata, path):
    """Export an explicitly supported fitted sklearn Pipeline to one JSON file.

    Export is a trusted in-process operation. Runtime loading never loads Python
    objects. Unsupported transformations/configuration fail before writing.
    """
    from sklearn.compose import ColumnTransformer
    from sklearn.ensemble import RandomForestClassifier, RandomForestRegressor
    from sklearn.linear_model import LogisticRegression
    from sklearn.pipeline import Pipeline
    from sklearn.preprocessing import OneHotEncoder, StandardScaler

    _require(Path(path).suffix.lower() == ".json", "Model artifact destination must end in .json")
    _require(type(pipeline) is Pipeline and len(pipeline.steps) == 2, "Expected two-step Pipeline")
    prep, estimator = [step[1] for step in pipeline.steps]
    _require(type(prep) is ColumnTransformer and hasattr(prep, "transformers_"), "Expected fitted ColumnTransformer")
    _require(not prep.transformer_weights, "Transformer weights are unsupported")
    _require(hasattr(prep, "feature_names_in_"), "Fit using a DataFrame with string columns")
    features = prep.feature_names_in_.tolist()
    blocks, width = [], 0
    configured = {name: transformer for name, transformer, _ in prep.transformers}
    configured["remainder"] = prep.remainder
    for name, transformer, columns in prep.transformers_:
        original = configured[name]
        if isinstance(original, str) and original == "drop":
            continue
        _require(not isinstance(columns, (str, int, np.integer)) and not callable(columns),
                 "Scalar/callable column selectors are unsupported")
        if isinstance(columns, slice):
            _require(all(v is None or type(v) is int for v in (columns.start, columns.stop, columns.step)),
                     "Only positional slices are supported")
            columns = features[columns]
        else:
            columns = list(columns)
            if columns and all(isinstance(c, (bool, np.bool_)) for c in columns):
                _require(len(columns) == len(features), "Invalid column mask")
                columns = [c for c, enabled in zip(features, columns) if enabled]
            elif columns and all(isinstance(c, (int, np.integer)) and not isinstance(c, (bool, np.bool_)) for c in columns):
                _require(all(-len(features) <= c < len(features) for c in columns), "Invalid column indices")
                columns = [features[c] for c in columns]
        if not columns:
            continue
        block = {"columns": columns}
        if isinstance(original, str) and original == "passthrough":
            block["kind"] = "passthrough"
        elif type(transformer) is StandardScaler:
            block.update(kind="standard_scaler",
                         mean=transformer.mean_.tolist() if transformer.with_mean else [0.0] * len(columns),
                         scale=transformer.scale_.tolist() if transformer.with_std else [1.0] * len(columns))
        elif type(transformer) is OneHotEncoder:
            _require(transformer.handle_unknown == "ignore" and transformer.drop is None
                     and transformer.min_frequency is None and transformer.max_categories is None
                     and np.dtype(transformer.dtype) == np.dtype(np.float64),
                     "Unsupported OneHotEncoder configuration")
            block.update(kind="one_hot", categories=[c.tolist() for c in transformer.categories_])
        else:
            raise ValueError("Unsupported preprocessor")
        width += sum(len(c) for c in block["categories"]) if block["kind"] == "one_hot" else len(columns)
        blocks.append(block)
    _require(getattr(estimator, "n_features_in_", None) == width, "Estimator feature count mismatch")
    if type(estimator) is LogisticRegression:
        _require(len(estimator.classes_) == 2 and estimator.coef_.shape == (1, width)
                 and getattr(estimator, "multi_class", "auto") != "multinomial",
                 "Only binary non-multinomial logistic regression is supported")
        est = {"kind": "logistic_regression", "classes": estimator.classes_.tolist(),
               "coef": estimator.coef_[0].tolist(), "intercept": estimator.intercept_.tolist()}
    elif type(estimator) in (RandomForestClassifier, RandomForestRegressor):
        _require(estimator.n_outputs_ == 1, "Multi-output forests are unsupported")
        classifier = type(estimator) is RandomForestClassifier
        est = {"kind": "random_forest_classifier" if classifier else "random_forest_regressor", "trees": []}
        if classifier:
            est["classes"] = estimator.classes_.tolist()
        for fitted in estimator.estimators_:
            tree = fitted.tree_
            est["trees"].append({"left": tree.children_left.tolist(), "right": tree.children_right.tolist(),
                                 "feature": tree.feature.tolist(), "threshold": tree.threshold.tolist(),
                                 "value": tree.value[:, 0, :].tolist()})
    else:
        raise ValueError("Unsupported estimator")
    artifact = {"schema_version": SCHEMA_VERSION, "metadata": metadata,
                "preprocessing": {"features": features, "transformers": blocks, "n_outputs": width},
                "estimator": est}
    _validate(artifact)
    Path(path).write_text(json.dumps(artifact, allow_nan=False, separators=(",", ":")) + "\n", encoding="utf-8")
