from fastapi.testclient import TestClient

from src.api import app
from src import predict


EMPLOYEE = {
    "Age": 30, "BusinessTravel": "Travel_Rarely", "DailyRate": 800,
    "Department": "Research & Development", "DistanceFromHome": 10, "Education": 3,
    "EducationField": "Life Sciences", "EnvironmentSatisfaction": 3, "Gender": "Male",
    "HourlyRate": 60, "JobInvolvement": 3, "JobLevel": 2, "JobRole": "Research Scientist",
    "JobSatisfaction": 3, "MaritalStatus": "Single", "MonthlyIncome": 5000,
    "MonthlyRate": 15000, "NumCompaniesWorked": 2, "OverTime": "Yes",
    "PercentSalaryHike": 15, "PerformanceRating": 3, "RelationshipSatisfaction": 3,
    "StockOptionLevel": 0, "TotalWorkingYears": 8, "TrainingTimesLastYear": 3,
    "WorkLifeBalance": 3, "YearsAtCompany": 3, "YearsInCurrentRole": 2,
    "YearsSinceLastPromotion": 1, "YearsWithCurrManager": 2,
}


def test_health_is_lightweight_and_ready_reports_models():
    client = TestClient(app)
    assert client.get("/health").status_code == 200
    response = client.get("/ready")
    assert response.status_code == 200
    assert response.json()["status"] == "ready"
    assert set(response.json()["models"]) == {"attrition", "salary", "performance", "promotion"}


def test_missing_attrition_artifact_is_a_safe_503(tmp_path, monkeypatch):
    monkeypatch.setattr(predict, "MODEL_PATH", tmp_path / "missing.json")
    predict._load_attrition_model.cache_clear()
    response = TestClient(app).post("/predict", json=EMPLOYEE)
    assert response.status_code == 503
    assert "missing.json" not in response.text


def test_out_of_range_and_domain_values_are_rejected():
    client = TestClient(app)
    invalid = dict(EMPLOYEE, Age=17)
    assert client.post("/predict", json=invalid).status_code == 422
    invalid = dict(EMPLOYEE, Department="Unknown")
    assert client.post("/predict", json=invalid).status_code == 422


def test_valid_attrition_prediction_still_works():
    response = TestClient(app).post("/predict", json=EMPLOYEE)
    assert response.status_code == 200
    assert response.json()["attrition_prediction"] in {"Yes", "No"}
    assert 0 <= response.json()["attrition_probability"] <= 1
