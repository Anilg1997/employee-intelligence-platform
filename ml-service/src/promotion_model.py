"""Transparent promotion-readiness portfolio classifier.

The IBM HR CSV has no promotion outcome.  This module therefore does not train
on, or imply, a historical promotion target.  It applies the documented demo
policy in docs/ml/promotion-model.md to current employee attributes.
"""
import json
import operator
from pathlib import Path

MODEL_PATH = Path(__file__).resolve().parent.parent / "models" / "employee_promotion_model.json"
ARTIFACT = json.loads(MODEL_PATH.read_text(encoding="utf-8"))
MODEL_VERSION = ARTIFACT["metadata"]["model_version"]
ALGORITHM = ARTIFACT["metadata"]["algorithm"]
LIMITATIONS = ARTIFACT["metadata"]["limitations"]
OPERATORS = {"gte": operator.ge, "lt": operator.lt}


def predict_promotion(employee_data: dict) -> dict:
    checks = {
        rule["name"]: bool(OPERATORS[rule["operator"]](employee_data[rule["feature"]], rule["value"]))
        for rule in ARTIFACT["rules"]
    }
    score = sum(checks.values()) / len(checks)
    ready = score >= ARTIFACT["threshold"]
    return {
        "promotion_prediction": "Ready" if ready else "Not ready",
        "promotion_probability": float(score),
        "rule_score": float(score),
        "rule_checks": checks,
        "model_version": MODEL_VERSION,
        "algorithm": ALGORITHM,
        "target_type": ARTIFACT["metadata"]["target_type"],
        "limitations": LIMITATIONS,
    }
