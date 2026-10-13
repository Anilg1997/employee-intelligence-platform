from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import Literal

from .predict import predict_attrition, ModelUnavailable, load_attrition_model
from .salary_model import predict_salary, SalaryModelUnavailable
from .performance_model import predict_performance, PerformanceModelUnavailable
from .promotion_model import predict_promotion, PromotionModelUnavailable, load_promotion_model
from .salary_model import load_salary_model
from .performance_model import load_performance_model

app = FastAPI(
    title="Employee Attrition Prediction API",
    description="API for predicting employee attrition risk",
    version="1.0.0"
)


class EmployeeData(BaseModel):
    Age: int = Field(..., ge=18, le=60); BusinessTravel: Literal["Travel_Rarely", "Travel_Frequently", "Non-Travel"]
    DailyRate: int = Field(..., ge=102, le=1499); Department: Literal["Sales", "Research & Development", "Human Resources"]
    DistanceFromHome: int = Field(..., ge=1, le=29); Education: int = Field(..., ge=1, le=5)
    EducationField: Literal["Life Sciences", "Medical", "Marketing", "Technical Degree", "Human Resources", "Other"]
    EnvironmentSatisfaction: int = Field(..., ge=1, le=4); Gender: Literal["Male", "Female"]
    HourlyRate: int = Field(..., ge=30, le=100); JobInvolvement: int = Field(..., ge=1, le=4)
    JobLevel: int = Field(..., ge=1, le=5); JobRole: Literal["Sales Executive", "Research Scientist", "Laboratory Technician", "Manufacturing Director", "Healthcare Representative", "Manager", "Sales Representative", "Research Director", "Human Resources"]
    JobSatisfaction: int = Field(..., ge=1, le=4); MaritalStatus: Literal["Single", "Married", "Divorced"]
    MonthlyIncome: int = Field(..., ge=1009, le=19999); MonthlyRate: int = Field(..., ge=2094, le=26999)
    NumCompaniesWorked: int = Field(..., ge=0, le=9); OverTime: Literal["Yes", "No"]
    PercentSalaryHike: int = Field(..., ge=11, le=25); PerformanceRating: int = Field(..., ge=1, le=4)
    RelationshipSatisfaction: int = Field(..., ge=1, le=4); StockOptionLevel: int = Field(..., ge=0, le=3)
    TotalWorkingYears: int = Field(..., ge=0, le=40); TrainingTimesLastYear: int = Field(..., ge=0, le=6)
    WorkLifeBalance: int = Field(..., ge=1, le=4); YearsAtCompany: int = Field(..., ge=0, le=40)
    YearsInCurrentRole: int = Field(..., ge=0, le=18); YearsSinceLastPromotion: int = Field(..., ge=0, le=15)
    YearsWithCurrManager: int = Field(..., ge=0, le=17)


class AttritionPredictionResponse(BaseModel):
    attrition_prediction: Literal["Yes", "No"]
    attrition_probability: float = Field(..., ge=0, le=1)
    model_version: str = Field(..., min_length=1)
    decision_threshold: float = Field(..., ge=0, le=1)
    dataset: str = Field(..., min_length=1)
    training_provenance: str = Field(..., min_length=1)
    limitations: list[str]


class SalaryData(BaseModel):
    Age: int = Field(..., ge=18, le=60); Department: Literal["Sales", "Research & Development", "Human Resources"]
    JobLevel: int = Field(..., ge=1, le=5); JobRole: Literal["Sales Executive", "Research Scientist", "Laboratory Technician", "Manufacturing Director", "Healthcare Representative", "Manager", "Sales Representative", "Research Director", "Human Resources"]
    TotalWorkingYears: int = Field(..., ge=0, le=40); YearsAtCompany: int = Field(..., ge=0, le=40)
    Education: int = Field(..., ge=1, le=5); PercentSalaryHike: int = Field(..., ge=11, le=25)
    PerformanceRating: int = Field(..., ge=1, le=4); Gender: Literal["Male", "Female"]
    MaritalStatus: Literal["Single", "Married", "Divorced"]; OverTime: Literal["Yes", "No"]
    StockOptionLevel: int = Field(..., ge=0, le=3)


class PerformanceData(BaseModel):
    Age: int = Field(..., ge=18, le=60); BusinessTravel: Literal["Travel_Rarely", "Travel_Frequently", "Non-Travel"]; DailyRate: int = Field(..., ge=102, le=1499); Department: Literal["Sales", "Research & Development", "Human Resources"]; DistanceFromHome: int = Field(..., ge=1, le=29); Education: int = Field(..., ge=1, le=5); EducationField: Literal["Life Sciences", "Medical", "Marketing", "Technical Degree", "Human Resources", "Other"]; EnvironmentSatisfaction: int = Field(..., ge=1, le=4); Gender: Literal["Male", "Female"]; HourlyRate: int = Field(..., ge=30, le=100); JobInvolvement: int = Field(..., ge=1, le=4); JobLevel: int = Field(..., ge=1, le=5); JobRole: Literal["Sales Executive", "Research Scientist", "Laboratory Technician", "Manufacturing Director", "Healthcare Representative", "Manager", "Sales Representative", "Research Director", "Human Resources"]; JobSatisfaction: int = Field(..., ge=1, le=4); MaritalStatus: Literal["Single", "Married", "Divorced"]; MonthlyIncome: int = Field(..., ge=1009, le=19999); MonthlyRate: int = Field(..., ge=2094, le=26999); NumCompaniesWorked: int = Field(..., ge=0, le=9); OverTime: Literal["Yes", "No"]; PercentSalaryHike: int = Field(..., ge=11, le=25); RelationshipSatisfaction: int = Field(..., ge=1, le=4); StockOptionLevel: int = Field(..., ge=0, le=3); TotalWorkingYears: int = Field(..., ge=0, le=40); TrainingTimesLastYear: int = Field(..., ge=0, le=6); WorkLifeBalance: int = Field(..., ge=1, le=4); YearsAtCompany: int = Field(..., ge=0, le=40); YearsInCurrentRole: int = Field(..., ge=0, le=18); YearsSinceLastPromotion: int = Field(..., ge=0, le=15); YearsWithCurrManager: int = Field(..., ge=0, le=17)

class PromotionData(PerformanceData):
    PerformanceRating: int = Field(..., ge=1, le=4)


@app.get("/")
def home():
    return {
        "message": "Employee Attrition Prediction API is running"
    }


@app.get("/health")
def health_check():
    return {
        "status": "healthy"
    }


@app.get("/ready")
def readiness_check():
    loaders = {"attrition": load_attrition_model,
               "salary": load_salary_model, "performance": load_performance_model,
               "promotion": load_promotion_model}
    statuses = {}
    for name, loader in loaders.items():
        try:
            loader()
            statuses[name] = "ready"
        except Exception:
            statuses[name] = "unavailable"
    payload = {"status": "ready" if all(v == "ready" for v in statuses.values()) else "not_ready", "models": statuses}
    if payload["status"] != "ready":
        raise HTTPException(status_code=503, detail=payload)
    return payload


@app.post("/predict", response_model=AttritionPredictionResponse)
def predict(employee: EmployeeData):
    try:
        return predict_attrition(employee.model_dump())
    except ModelUnavailable as exc:
        raise HTTPException(status_code=503, detail="Attrition model unavailable. Contact the service operator.") from exc


@app.post("/predict/salary")
def predict_salary_endpoint(employee: SalaryData):
    try:
        return predict_salary(employee.model_dump())
    except SalaryModelUnavailable as exc:
        raise HTTPException(status_code=503, detail="Salary model unavailable. Contact the service operator.") from exc


@app.post("/predict/performance")
def predict_performance_endpoint(employee: PerformanceData):
    try:
        return predict_performance(employee.model_dump())
    except PerformanceModelUnavailable as exc:
        raise HTTPException(status_code=503, detail="Performance model unavailable. Contact the service operator.") from exc


@app.post("/predict/promotion")
def predict_promotion_endpoint(employee: PromotionData):
    try:
        return predict_promotion(employee.model_dump())
    except PromotionModelUnavailable as exc:
        raise HTTPException(status_code=503, detail="Promotion model unavailable. Contact the service operator.") from exc
