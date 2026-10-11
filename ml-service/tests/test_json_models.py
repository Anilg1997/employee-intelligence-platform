"""Parity and fail-closed checks using tiny synthetic fitted fixtures only."""
import json
from pathlib import Path

import numpy as np
import pandas as pd
import pytest
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier, RandomForestRegressor
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

from src.json_models import JsonPredictor, export_pipeline, load_artifact


def test_repository_models_are_json_only():
    models = Path(__file__).resolve().parents[1] / "models"
    assert {path.name for path in models.iterdir() if path.is_file()} == {
        "employee_attrition_model.json", "employee_salary_model.json",
        "employee_performance_model.json", "employee_promotion_model.json",
    }


def test_export_rejects_binary_filename(tmp_path):
    pipeline, _ = fixture_pipeline(LogisticRegression())
    with pytest.raises(ValueError, match="must end in .json"):
        export_pipeline(pipeline, {}, tmp_path / "model.pkl")
    assert not list(tmp_path.iterdir())


def fixture_pipeline(estimator, scaler=True):
    frame = pd.DataFrame({"number": [0., 1., 2., 3., 4., 5., 6., 7.],
                          "category": ["a", "b", "c", "a", "b", "c", "a", "b"],
                          "other": [3., 2., 1., 0., 1., 2., 3., 4.]})
    prep = ColumnTransformer([
        ("numeric", StandardScaler() if scaler else "passthrough", ["number", "other"]),
        ("category", OneHotEncoder(handle_unknown="ignore"), ["category"]),
    ])
    pipeline = Pipeline([("preprocessor", prep), ("estimator", estimator)])
    target = np.array(["stay", "stay", "leave", "stay", "leave", "leave", "leave", "stay"])
    if isinstance(estimator, RandomForestRegressor):
        target = np.array([1., 3., 4., 9., 7., 11., 10., 15.])
    pipeline.fit(frame, target)
    return pipeline, frame


@pytest.mark.parametrize("estimator", [LogisticRegression(), RandomForestClassifier(n_estimators=7, random_state=2),
                                       RandomForestRegressor(n_estimators=7, random_state=2)])
@pytest.mark.parametrize("scaler", [True, False])
def test_round_trip_parity(tmp_path, estimator, scaler):
    pipeline, frame = fixture_pipeline(estimator, scaler)
    path = tmp_path / "artifact.json"
    metadata = {"version": "fixture", "metrics": {"rows": 8}, "limitations": []}
    export_pipeline(pipeline, metadata, path)
    artifact = load_artifact(path)
    assert artifact["metadata"] == metadata
    assert list(tmp_path.iterdir()) == [path]
    query = pd.concat([frame, pd.DataFrame({"number": [2.5, -100.], "category": ["unseen", "a"], "other": [1., 2.]})], ignore_index=True)
    model = artifact["model"]
    expected = pipeline.predict(query)
    if isinstance(estimator, RandomForestRegressor):
        np.testing.assert_allclose(model.predict(query), expected, rtol=1e-14)
        with pytest.raises(ValueError):
            model.predict_proba(query)
    else:
        np.testing.assert_array_equal(model.classes_, estimator.classes_)
        np.testing.assert_array_equal(model.predict(query), expected)
        np.testing.assert_allclose(model.predict_proba(query), pipeline.predict_proba(query), rtol=1e-14, atol=1e-15)
    # DataFrame names, not the provided column order, determine preprocessing.
    np.testing.assert_array_equal(model.predict(query), model.predict(query[query.columns[::-1]]))


def artifact_fixture(tmp_path):
    pipeline, frame = fixture_pipeline(RandomForestClassifier(n_estimators=2, random_state=2))
    path = tmp_path / "model.json"
    export_pipeline(pipeline, {}, path)
    return json.loads(path.read_text()), path, frame


@pytest.mark.parametrize("mutation", [
    lambda a: a.update(schema_version=2),
    lambda a: a.update(schema_version=True),
    lambda a: a.update(payload="not allowed"),
    lambda a: a["metadata"].update(value=float("nan")),
    lambda a: a["preprocessing"].update(n_outputs=999),
    lambda a: a["preprocessing"]["transformers"][0]["scale"].__setitem__(0, 0),
    lambda a: a["estimator"].update(kind="arbitrary.class"),
    lambda a: a["estimator"].update(classes=["same", "same"]),
    lambda a: a["estimator"]["trees"][0]["left"].__setitem__(0, 0),
    lambda a: a["estimator"]["trees"][0]["left"].__setitem__(0, 10000),
    lambda a: a["estimator"]["trees"][0]["left"].__setitem__(0, -2),
    lambda a: a["estimator"]["trees"][0]["left"].__setitem__(0, 2**64 - 1),
    lambda a: a["estimator"]["trees"][0]["right"].__setitem__(0, a["estimator"]["trees"][0]["left"][0]),
    lambda a: a["estimator"]["trees"][0]["feature"].__setitem__(0, 10000),
    lambda a: a["estimator"]["trees"][0]["threshold"].__setitem__(0, float("inf")),
    lambda a: a["estimator"]["trees"][0]["value"].__setitem__(0, [-1, 1]),
])
def test_malformed_artifacts_rejected(tmp_path, mutation):
    artifact, path, _ = artifact_fixture(tmp_path)
    mutation(artifact)
    path.write_text(json.dumps(artifact))
    with pytest.raises(ValueError):
        load_artifact(path)


@pytest.mark.parametrize("text", ['{"schema_version":1,"schema_version":1}', '{invalid', 'null', '[]'])
def test_malformed_json(tmp_path, text):
    path = tmp_path / "bad.json"
    path.write_text(text)
    with pytest.raises(ValueError):
        load_artifact(path)


def test_invalid_inputs(tmp_path):
    artifact, _, frame = artifact_fixture(tmp_path)
    model = JsonPredictor(artifact)
    for bad in [frame.drop(columns="number"), frame.assign(number=np.nan), frame.assign(number=np.inf),
                frame.assign(category=None), pd.concat([frame, frame[["number"]]], axis=1)]:
        with pytest.raises(ValueError):
            model.predict(bad)


@pytest.mark.parametrize("change", [
    lambda p: setattr(p, "transformer_weights", {"numeric": 2}),
    lambda p: setattr(p.named_transformers_["category"], "drop", "first"),
    lambda p: setattr(p.named_transformers_["category"], "handle_unknown", "error"),
    lambda p: setattr(p.named_transformers_["category"], "min_frequency", 2),
    lambda p: setattr(p.named_transformers_["category"], "max_categories", 2),
    lambda p: setattr(p.named_transformers_["category"], "dtype", np.float32),
])
def test_unsupported_configuration_rejected(tmp_path, change):
    pipeline, _ = fixture_pipeline(LogisticRegression())
    change(pipeline.steps[0][1])
    with pytest.raises(ValueError):
        export_pipeline(pipeline, {}, tmp_path / "rejected.json")
    assert not (tmp_path / "rejected.json").exists()


def test_float32_threshold_parity(tmp_path):
    frame = pd.DataFrame({"x": [0., 1., 2., 3.]})
    pipeline = Pipeline([("prep", ColumnTransformer([("x", "passthrough", ["x"])])),
                         ("model", RandomForestRegressor(n_estimators=1, bootstrap=False, random_state=1))])
    pipeline.fit(frame, [0., 10., 20., 30.])
    path = tmp_path / "threshold.json"
    export_pipeline(pipeline, {}, path)
    thresholds = pipeline.steps[-1][1].estimators_[0].tree_.threshold
    query = pd.DataFrame({"x": [np.nextafter(t, np.inf) for t in thresholds if t >= 0]})
    np.testing.assert_array_equal(load_artifact(path)["model"].predict(query), pipeline.predict(query))


@pytest.mark.parametrize("with_mean,with_std", [(False, False), (True, False), (False, True)])
def test_scaler_flags_and_remainder(tmp_path, with_mean, with_std):
    frame = pd.DataFrame({"x": [0., 1., 2., 3.], "y": [1., 2., 1., 4.]})
    pipeline = Pipeline([("prep", ColumnTransformer([("x", StandardScaler(with_mean=with_mean, with_std=with_std), [0])], remainder="passthrough")),
                         ("model", LogisticRegression())])
    pipeline.fit(frame, [0, 1, 0, 1])
    path = tmp_path / "flags.json"
    export_pipeline(pipeline, {}, path)
    np.testing.assert_allclose(load_artifact(path)["model"].predict_proba(frame), pipeline.predict_proba(frame), atol=1e-15)


def test_multiclass_forest_and_numeric_categories(tmp_path):
    frame = pd.DataFrame({"x": [0., 1., 2., 3., 4., 5.], "cat": [1, 2, 3, 1, 2, 3]})
    pipeline = Pipeline([("prep", ColumnTransformer([
        ("numeric", "passthrough", ["x"]),
        ("categorical", OneHotEncoder(handle_unknown="ignore"), ["cat"])])),
        ("model", RandomForestClassifier(n_estimators=5, random_state=2))])
    pipeline.fit(frame, [10, 20, 30, 10, 20, 30])
    path = tmp_path / "multiclass.json"
    export_pipeline(pipeline, {}, path)
    query = pd.concat([frame, pd.DataFrame({"x": [1.5], "cat": [99]})], ignore_index=True)
    model = load_artifact(path)["model"]
    np.testing.assert_array_equal(model.classes_, [10, 20, 30])
    np.testing.assert_allclose(model.predict_proba(query), pipeline.predict_proba(query), atol=1e-15)
    np.testing.assert_array_equal(model.predict(query), pipeline.predict(query))


def test_disconnected_node_rejected(tmp_path):
    artifact, _, _ = artifact_fixture(tmp_path)
    tree = artifact["estimator"]["trees"][0]
    for key, value in {"left": -1, "right": -1, "feature": -2, "threshold": -2., "value": [1., 0.]}.items():
        tree[key].append(value)
    with pytest.raises(ValueError, match="Unreachable"):
        JsonPredictor(artifact)


def test_float32_scaler_parity(tmp_path):
    pipeline, frame = fixture_pipeline(LogisticRegression())
    frame[["number", "other"]] = frame[["number", "other"]].astype(np.float32)
    path = tmp_path / "float32.json"
    export_pipeline(pipeline, {}, path)
    np.testing.assert_allclose(load_artifact(path)["model"].predict_proba(frame), pipeline.predict_proba(frame), rtol=1e-14, atol=1e-15)
