"""Transparent promotion-readiness portfolio classifier.

The IBM HR CSV has no promotion outcome.  This module therefore does not train
on, or imply, a historical promotion target.  It applies the documented demo
policy in docs/ml/promotion-model.md to current employee attributes.
"""
import json
import operator
import math
from pathlib import Path
from functools import lru_cache

MODEL_PATH = Path(__file__).resolve().parent.parent / "models" / "employee_promotion_model.json"
OPERATORS = {"gte": operator.ge, "lt": operator.lt}
PROMOTION_FEATURES = frozenset({
    "Age", "BusinessTravel", "DailyRate", "Department", "DistanceFromHome", "Education",
    "EducationField", "EnvironmentSatisfaction", "Gender", "HourlyRate", "JobInvolvement",
    "JobLevel", "JobRole", "JobSatisfaction", "MaritalStatus", "MonthlyIncome", "MonthlyRate",
    "NumCompaniesWorked", "OverTime", "PercentSalaryHike", "PerformanceRating",
    "RelationshipSatisfaction", "StockOptionLevel", "TotalWorkingYears", "TrainingTimesLastYear",
    "WorkLifeBalance", "YearsAtCompany", "YearsInCurrentRole", "YearsSinceLastPromotion",
    "YearsWithCurrManager"
})
NUMERIC_FEATURES = frozenset({"Age", "DailyRate", "DistanceFromHome", "Education", "EnvironmentSatisfaction", "HourlyRate", "JobInvolvement", "JobLevel", "JobSatisfaction", "MonthlyIncome", "MonthlyRate", "NumCompaniesWorked", "PercentSalaryHike", "PerformanceRating", "RelationshipSatisfaction", "StockOptionLevel", "TotalWorkingYears", "TrainingTimesLastYear", "WorkLifeBalance", "YearsAtCompany", "YearsInCurrentRole", "YearsSinceLastPromotion", "YearsWithCurrManager"})


class PromotionModelUnavailable(RuntimeError):
    pass


@lru_cache(maxsize=4)
def _load_promotion_model(path):
    try:
        artifact = json.loads(Path(path).read_text(encoding="utf-8"))
        metadata = artifact["metadata"]
        rules = artifact["rules"]
        threshold = artifact["threshold"]
        required_metadata = {"model_version", "algorithm", "target_type", "trained", "limitations"}
        if (not isinstance(metadata, dict) or not required_metadata <= metadata.keys()
                or not isinstance(rules, list) or not rules
                or isinstance(threshold, bool) or not isinstance(threshold, (int, float))
                or not math.isfinite(threshold) or not 0 <= threshold <= 1
                or metadata["algorithm"] != "deterministic-business-rules"
                or metadata["target_type"] != "portfolio_demo_rule_target"
                or metadata["trained"] is not False
                or not isinstance(metadata["model_version"], str) or not metadata["model_version"].strip()
                or not isinstance(metadata["limitations"], list) or not metadata["limitations"]
                or any(not isinstance(item, str) or not item.strip() for item in metadata["limitations"])
                or artifact.get("score") != "fraction_of_checks_passed"):
            raise ValueError("invalid promotion artifact")
        names = set()
        for rule in rules:
            if (not isinstance(rule, dict) or rule.get("operator") not in OPERATORS
                    or not isinstance(rule.get("name"), str) or not rule["name"].strip()
                    or rule["name"] in names or not isinstance(rule.get("feature"), str)
                    or rule["feature"] not in NUMERIC_FEATURES or "value" not in rule
                    or isinstance(rule["value"], bool) or not isinstance(rule["value"], (int, float))
                    or not math.isfinite(rule["value"])
                    or set(rule) != {"name", "feature", "operator", "value"}):
                raise ValueError("invalid promotion rule")
            names.add(rule["name"])
        return artifact
    except Exception as exc:
        raise PromotionModelUnavailable("Promotion model unavailable.") from exc


def load_promotion_model():
    return _load_promotion_model(str(MODEL_PATH))


def predict_promotion(employee_data: dict) -> dict:
    artifact = load_promotion_model()
    metadata = artifact["metadata"]
    rule_features = {rule["feature"] for rule in artifact["rules"]}
    if not isinstance(employee_data, dict) or not rule_features <= set(employee_data) <= PROMOTION_FEATURES:
        raise ValueError("Promotion input must contain exactly the aligned rule features.")
    if any(not isinstance(employee_data[feature], (int, float)) or isinstance(employee_data[feature], bool)
           or not math.isfinite(employee_data[feature]) for feature in rule_features):
        raise ValueError("Promotion input contains invalid feature values.")
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
        "decision_threshold": float(artifact["threshold"]),
        "limitations": metadata["limitations"],
    }
