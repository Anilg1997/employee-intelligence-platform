import pytest
from fastapi.testclient import TestClient

from src.api import app


@pytest.fixture
def client():
    return TestClient(app)


@pytest.fixture
def employee():
    return {
        "Age": 30,
        "BusinessTravel": "Travel_Rarely",
        "DailyRate": 800,
        "Department": "Research & Development",
        "DistanceFromHome": 10,
        "Education": 3,
        "EducationField": "Life Sciences",
        "EnvironmentSatisfaction": 3,
        "Gender": "Male",
        "HourlyRate": 60,
        "JobInvolvement": 3,
        "JobLevel": 2,
        "JobRole": "Research Scientist",
        "JobSatisfaction": 3,
        "MaritalStatus": "Single",
        "MonthlyIncome": 5000,
        "MonthlyRate": 15000,
        "NumCompaniesWorked": 2,
        "OverTime": "Yes",
        "PercentSalaryHike": 15,
        "PerformanceRating": 3,
        "RelationshipSatisfaction": 3,
        "StockOptionLevel": 0,
        "TotalWorkingYears": 8,
        "TrainingTimesLastYear": 3,
        "WorkLifeBalance": 3,
        "YearsAtCompany": 3,
        "YearsInCurrentRole": 2,
        "YearsSinceLastPromotion": 1,
        "YearsWithCurrManager": 2,
    }


@pytest.mark.parametrize(
    "rating, prediction, score, performance_check",
    [(3, "Ready", 0.75, True), (2, "Not ready", 0.5, False)],
)
def test_promotion_request_uses_performance_rating(
    client, employee, rating, prediction, score, performance_check
):
    employee["PerformanceRating"] = rating
    response = client.post("/predict/promotion", json=employee)

    assert response.status_code == 200
    result = response.json()
    assert result["promotion_prediction"] == prediction
    assert result["promotion_probability"] == result["rule_score"] == score
    assert result["rule_checks"] == {
        "tenure": True,
        "time_since_promotion": False,
        "performance": performance_check,
        "level_headroom": True,
    }
    assert result["algorithm"] == "deterministic-business-rules"
    assert result["target_type"] == "portfolio_demo_rule_target"
    assert any("No promotion outcome" in item for item in result["limitations"])


@pytest.mark.parametrize(
    "field",
    ["PerformanceRating", "YearsAtCompany", "YearsSinceLastPromotion", "JobLevel"],
)
def test_promotion_rejects_missing_rule_inputs(client, employee, field):
    del employee[field]
    response = client.post("/predict/promotion", json=employee)

    assert response.status_code == 422
    assert any(error["loc"] == ["body", field] for error in response.json()["detail"])


@pytest.mark.parametrize(
    "field",
    ["PerformanceRating", "YearsAtCompany", "YearsSinceLastPromotion", "JobLevel"],
)
@pytest.mark.parametrize("value", [None, "invalid", 2.5, [], {}])
def test_promotion_rejects_invalid_rule_inputs(client, employee, field, value):
    employee[field] = value
    response = client.post("/predict/promotion", json=employee)

    assert response.status_code == 422
    assert any(error["loc"] == ["body", field] for error in response.json()["detail"])


def test_promotion_schema_requires_rating_independently_of_performance(client):
    schema = client.get("/openapi.json").json()
    request_schema = schema["paths"]["/predict/promotion"]["post"]["requestBody"]["content"]["application/json"]["schema"]
    promotion = schema["components"]["schemas"][request_schema["$ref"].split("/")[-1]]
    assert "PerformanceRating" in promotion["required"]
    assert promotion["properties"]["PerformanceRating"]["type"] == "integer"
    assert "PerformanceRating" not in schema["components"]["schemas"]["PerformanceData"]["properties"]
