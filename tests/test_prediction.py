import sys
from pathlib import Path

import joblib


# Add src directory to Python path
PROJECT_ROOT = Path(__file__).resolve().parent.parent
SRC_DIR = PROJECT_ROOT / "src"

sys.path.insert(0, str(SRC_DIR))

from predict import predict_attrition


MODEL_PATH = PROJECT_ROOT / "models" / "employee_attrition_model.pkl"


sample_employee = {
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
    "YearsWithCurrManager": 2
}


def test_model_exists():

    assert MODEL_PATH.exists()


def test_model_can_be_loaded():

    model = joblib.load(MODEL_PATH)

    assert model is not None


def test_prediction_result():

    result = predict_attrition(sample_employee)

    assert "attrition_prediction" in result
    assert "attrition_probability" in result


def test_prediction_value():

    result = predict_attrition(sample_employee)

    assert result["attrition_prediction"] in ["Yes", "No"]


def test_probability_range():

    result = predict_attrition(sample_employee)

    probability = result["attrition_probability"]

    assert 0 <= probability <= 1