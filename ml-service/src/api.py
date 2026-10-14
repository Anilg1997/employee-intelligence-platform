from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, ConfigDict, Field, model_validator
from typing import Literal

from .predict import predict_attrition, ModelUnavailable, load_attrition_model
from .salary_model import predict_salary, SalaryModelUnavailable
from .performance_model import predict_performance, PerformanceModelUnavailable
from .promotion_model import predict_promotion, PromotionModelUnavailable, load_promotion_model
from .salary_model import load_salary_model
from .salary_contract import SalaryPredictionResponse
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
    model_config = ConfigDict(strict=True, extra="forbid")
    Age: int = Field(..., ge=18, le=60); Department: Literal["Sales", "Research & Development", "Human Resources"]
    JobLevel: int = Field(..., ge=1, le=5); JobRole: Literal["Sales Executive", "Research Scientist", "Laboratory Technician", "Manufacturing Director", "Healthcare Representative", "Manager", "Sales Representative", "Research Director", "Human Resources"]
    TotalWorkingYears: int = Field(..., ge=0, le=40); YearsAtCompany: int = Field(..., ge=0, le=40)
    Education: int = Field(..., ge=1, le=5); PercentSalaryHike: int = Field(..., ge=11, le=25)
    PerformanceRating: int = Field(..., ge=1, le=4); Gender: Literal["Male", "Female"]
    MaritalStatus: Literal["Single", "Married", "Divorced"]; OverTime: Literal["Yes", "No"]
    StockOptionLevel: int = Field(..., ge=0, le=3)


class PerformanceData(BaseModel):
    model_config = ConfigDict(strict=True, extra="forbid")
    Age: int = Field(..., ge=18, le=60); BusinessTravel: Literal["Travel_Rarely", "Travel_Frequently", "Non-Travel"]; DailyRate: int = Field(..., ge=102, le=1499); Department: Literal["Sales", "Research & Development", "Human Resources"]; DistanceFromHome: int = Field(..., ge=1, le=29); Education: int = Field(..., ge=1, le=5); EducationField: Literal["Life Sciences", "Medical", "Marketing", "Technical Degree", "Human Resources", "Other"]; EnvironmentSatisfaction: int = Field(..., ge=1, le=4); Gender: Literal["Male", "Female"]; HourlyRate: int = Field(..., ge=30, le=100); JobInvolvement: int = Field(..., ge=1, le=4); JobLevel: int = Field(..., ge=1, le=5); JobRole: Literal["Sales Executive", "Research Scientist", "Laboratory Technician", "Manufacturing Director", "Healthcare Representative", "Manager", "Sales Representative", "Research Director", "Human Resources"]; JobSatisfaction: int = Field(..., ge=1, le=4); MaritalStatus: Literal["Single", "Married", "Divorced"]; MonthlyIncome: int = Field(..., ge=1009, le=19999); MonthlyRate: int = Field(..., ge=2094, le=26999); NumCompaniesWorked: int = Field(..., ge=0, le=9); OverTime: Literal["Yes", "No"]; PercentSalaryHike: int = Field(..., ge=11, le=25); RelationshipSatisfaction: int = Field(..., ge=1, le=4); StockOptionLevel: int = Field(..., ge=0, le=3); TotalWorkingYears: int = Field(..., ge=0, le=40); TrainingTimesLastYear: int = Field(..., ge=0, le=6); WorkLifeBalance: int = Field(..., ge=1, le=4); YearsAtCompany: int = Field(..., ge=0, le=40); YearsInCurrentRole: int = Field(..., ge=0, le=18); YearsSinceLastPromotion: int = Field(..., ge=0, le=15); YearsWithCurrManager: int = Field(..., ge=0, le=17)

class PromotionData(PerformanceData):
    model_config = ConfigDict(strict=True, extra="forbid")
    PerformanceRating: int = Field(..., ge=1, le=4)


class PromotionPredictionResponse(BaseModel):
    model_config = ConfigDict(strict=True, extra="forbid")
    promotion_prediction: Literal["Ready", "Not ready"]
    promotion_probability: float = Field(..., ge=0, le=1)
    rule_score: float = Field(..., ge=0, le=1)
    rule_checks: dict[str, bool] = Field(..., min_length=1)
    model_version: str = Field(..., min_length=1)
    algorithm: Literal["deterministic-business-rules"]
    target_type: Literal["portfolio_demo_rule_target"]
    decision_threshold: float = Field(..., ge=0, le=1)
    limitations: list[str] = Field(..., min_length=1)

    @model_validator(mode="after")
    def validate_contract(self):
        if (set(self.rule_checks) != {"tenure", "time_since_promotion", "performance", "level_headroom"}
                or any(not isinstance(value, bool) for value in self.rule_checks.values())
                or abs(self.rule_score - sum(self.rule_checks.values()) / len(self.rule_checks)) > 1e-6
                or abs(self.promotion_probability - self.rule_score) > 1e-6
                or self.promotion_prediction != ("Ready" if self.rule_score >= self.decision_threshold else "Not ready")
                or any(not isinstance(item, str) or not item.strip() for item in self.limitations)
                or not any("not a calibrated probability" in item.lower() for item in self.limitations)):
            raise ValueError("Invalid promotion prediction contract")
        return self


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


@app.post("/predict/salary", response_model=SalaryPredictionResponse)
def predict_salary_endpoint(employee: SalaryData):
    try:
        return predict_salary(employee.model_dump())
    except SalaryModelUnavailable as exc:
        raise HTTPException(status_code=503, detail="Salary model unavailable. Contact the service operator.") from exc


class PerformancePredictionResponse(BaseModel):
    model_config = ConfigDict(strict=True, extra="forbid")
    performance_prediction: Literal[1, 2, 3, 4]
    performance_probability: float = Field(..., ge=0, le=1)
    class_probabilities: dict[str, float]
    model_version: str = Field(..., min_length=1)
    algorithm: str = Field(..., min_length=1)
    dataset: str = Field(..., min_length=1)
    training_provenance: str = Field(..., min_length=1)
    metrics: dict[str, dict[str, float]]
    limitations: list[str] = Field(..., min_length=1)

    @model_validator(mode="after")
    def validate_contract(self):
        if (not self.class_probabilities or not set(self.class_probabilities) <= {"1", "2", "3", "4"}
                or any(not 0 <= probability <= 1 for probability in self.class_probabilities.values())
                or abs(sum(self.class_probabilities.values()) - 1.0) > 1e-6
                or abs(self.performance_probability - self.class_probabilities.get(str(self.performance_prediction), -1)) > 1e-6
                or set(self.metrics) != {"validation", "test"}
                or any(set(values) != {"accuracy", "balanced_accuracy", "f1_macro"}
                       or any(not 0 <= metric <= 1 for metric in values.values()) for values in self.metrics.values())
                or any(not item.strip() for item in self.limitations)):
            raise ValueError("Invalid performance prediction contract")
        return self


@app.post("/predict/performance", response_model=PerformancePredictionResponse)
def predict_performance_endpoint(employee: PerformanceData):
    try:
        return predict_performance(employee.model_dump())
    except PerformanceModelUnavailable as exc:
        raise HTTPException(status_code=503, detail="Performance model unavailable. Contact the service operator.") from exc


@app.post("/predict/promotion", response_model=PromotionPredictionResponse)
def predict_promotion_endpoint(employee: PromotionData):
    try:
        return predict_promotion(employee.model_dump())
    except PromotionModelUnavailable as exc:
        raise HTTPException(status_code=503, detail="Promotion model unavailable. Contact the service operator.") from exc
