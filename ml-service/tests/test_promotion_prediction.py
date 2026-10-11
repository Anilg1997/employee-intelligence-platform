import sys
import json
import itertools
from pathlib import Path
import pytest
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))
from promotion_model import MODEL_PATH, predict_promotion


def test_demo_rules_are_explicit_and_deterministic():
    row = {"YearsAtCompany": 4, "YearsSinceLastPromotion": 3, "PerformanceRating": 3, "JobLevel": 2}
    result = predict_promotion(row)
    assert result["promotion_prediction"] == "Ready"
    assert result["promotion_probability"] == 1.0
    assert result["target_type"] == "portfolio_demo_rule_target"
    assert result["algorithm"] == "deterministic-business-rules"


def test_rule_classifier_does_not_claim_ground_truth():
    result = predict_promotion({"YearsAtCompany": 1, "YearsSinceLastPromotion": 0, "PerformanceRating": 4, "JobLevel": 1})
    assert result["promotion_prediction"] == "Not ready"
    assert any("No promotion outcome" in limitation for limitation in result["limitations"])


def test_promotion_json_contains_rules_and_honest_metadata():
    artifact = json.loads(MODEL_PATH.read_text(encoding="utf-8"))
    assert artifact["metadata"]["algorithm"] == "deterministic-business-rules"
    assert artifact["metadata"]["trained"] is False
    assert artifact["metadata"]["target_type"] == "portfolio_demo_rule_target"
    assert len(artifact["rules"]) == 4
    assert artifact["threshold"] == 0.75


@pytest.mark.parametrize("tenure,since,rating,level", itertools.product([1, 2], [1, 2], [2, 3], [4, 5]))
def test_all_rule_boundaries_preserve_score_and_threshold(tenure, since, rating, level):
    result = predict_promotion({"YearsAtCompany": tenure, "YearsSinceLastPromotion": since,
                                "PerformanceRating": rating, "JobLevel": level})
    checks = {"tenure": tenure >= 2, "time_since_promotion": since >= 2,
              "performance": rating >= 3, "level_headroom": level < 5}
    score = sum(checks.values()) / 4
    assert result["rule_checks"] == checks
    assert result["promotion_probability"] == result["rule_score"] == score
    assert result["promotion_prediction"] == ("Ready" if score >= 0.75 else "Not ready")
