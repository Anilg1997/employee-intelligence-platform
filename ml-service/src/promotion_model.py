"""Transparent promotion-readiness portfolio classifier.

The IBM HR CSV has no promotion outcome.  This module therefore does not train
on, or imply, a historical promotion target.  It applies the documented demo
policy in docs/ml/promotion-model.md to current employee attributes.
"""
MODEL_VERSION = "promotion-readiness-rules-v1"
ALGORITHM = "deterministic-business-rules"
LIMITATIONS = [
    "No promotion outcome exists in the IBM HR dataset; this is a portfolio/demo target, not ground truth.",
    "The score is a rule score, not a calibrated probability or likelihood of promotion.",
    "Synthetic IBM HR data and policy assumptions are not representative of a real employer.",
    "This estimate must not be used as a standalone employment or promotion decision.",
]


def predict_promotion(employee_data: dict) -> dict:
    checks = {
        "tenure": employee_data["YearsAtCompany"] >= 2,
        "time_since_promotion": employee_data["YearsSinceLastPromotion"] >= 2,
        "performance": employee_data["PerformanceRating"] >= 3,
        "level_headroom": employee_data["JobLevel"] < 5,
    }
    score = sum(checks.values()) / len(checks)
    ready = score >= 0.75
    return {
        "promotion_prediction": "Ready" if ready else "Not ready",
        "promotion_probability": float(score),
        "rule_score": float(score),
        "rule_checks": checks,
        "model_version": MODEL_VERSION,
        "algorithm": ALGORITHM,
        "target_type": "portfolio_demo_rule_target",
        "limitations": LIMITATIONS,
    }
