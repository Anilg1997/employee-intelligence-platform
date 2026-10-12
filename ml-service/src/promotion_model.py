"""Transparent promotion-readiness portfolio classifier.

The IBM HR CSV has no promotion outcome.  This module therefore does not train
on, or imply, a historical promotion target.  It applies the documented demo
policy in docs/ml/promotion-model.md to current employee attributes.
"""
import json
import operator
from pathlib import Path
from functools import lru_cache

MODEL_PATH = Path(__file__).resolve().parent.parent / "models" / "employee_promotion_model.json"
OPERATORS = {"gte": operator.ge, "lt": operator.lt}


class PromotionModelUnavailable(RuntimeError):
    pass


@lru_cache(maxsize=4)
def _load_promotion_model(path):
    try:
        artifact = json.loads(Path(path).read_text(encoding="utf-8"))
        metadata = artifact["metadata"]
        rules = artifact["rules"]
        if (not isinstance(metadata, dict) or not isinstance(rules, list) or not rules
                or not isinstance(artifact["threshold"], (int, float))
                or not 0 <= artifact["threshold"] <= 1):
            raise ValueError("invalid promotion artifact")
        for rule in rules:
            if (not isinstance(rule, dict) or rule.get("operator") not in OPERATORS
                    or not isinstance(rule.get("feature"), str) or "value" not in rule):
                raise ValueError("invalid promotion rule")
        return artifact
    except Exception as exc:
        raise PromotionModelUnavailable("Promotion model unavailable.") from exc


def load_promotion_model():
    return _load_promotion_model(str(MODEL_PATH))


def predict_promotion(employee_data: dict) -> dict:
    artifact = load_promotion_model()
    metadata = artifact["metadata"]
    checks = {
        rule["name"]: bool(OPERATORS[rule["operator"]](employee_data[rule["feature"]], rule["value"]))
        for rule in artifact["rules"]
    }
    score = sum(checks.values()) / len(checks)
    ready = score >= artifact["threshold"]
    return {
        "promotion_prediction": "Ready" if ready else "Not ready",
        "promotion_probability": float(score),
        "rule_score": float(score),
        "rule_checks": checks,
        "model_version": metadata["model_version"],
        "algorithm": metadata["algorithm"],
        "target_type": metadata["target_type"],
        "limitations": metadata["limitations"],
    }
