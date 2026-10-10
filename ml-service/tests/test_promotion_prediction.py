import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))
from promotion_model import predict_promotion


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
