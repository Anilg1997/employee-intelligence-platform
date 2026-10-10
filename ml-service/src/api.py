from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from .predict import predict_attrition
from .salary_model import predict_salary, SalaryModelUnavailable
from .performance_model import predict_performance, PerformanceModelUnavailable
from .promotion_model import predict_promotion

app = FastAPI(
    title="Employee Attrition Prediction API",
    description="API for predicting employee attrition risk",
    version="1.0.0"
)


class EmployeeData(BaseModel):
    Age: int
    BusinessTravel: str
    DailyRate: int
    Department: str
    DistanceFromHome: int
    Education: int
    EducationField: str
    EnvironmentSatisfaction: int
    Gender: str
    HourlyRate: int
    JobInvolvement: int
    JobLevel: int
    JobRole: str
    JobSatisfaction: int
    MaritalStatus: str
    MonthlyIncome: int
    MonthlyRate: int
    NumCompaniesWorked: int
    OverTime: str
    PercentSalaryHike: int
    PerformanceRating: int
    RelationshipSatisfaction: int
    StockOptionLevel: int
    TotalWorkingYears: int
    TrainingTimesLastYear: int
    WorkLifeBalance: int
    YearsAtCompany: int
    YearsInCurrentRole: int
    YearsSinceLastPromotion: int
    YearsWithCurrManager: int


class SalaryData(BaseModel):
    Age: int
    Department: str
    JobLevel: int
    JobRole: str
    TotalWorkingYears: int
    YearsAtCompany: int
    Education: int
    PercentSalaryHike: int
    PerformanceRating: int
    Gender: str
    MaritalStatus: str
    OverTime: str
    StockOptionLevel: int


class PerformanceData(BaseModel):
    Age: int; BusinessTravel: str; DailyRate: int; Department: str; DistanceFromHome: int; Education: int; EducationField: str; EnvironmentSatisfaction: int; Gender: str; HourlyRate: int; JobInvolvement: int; JobLevel: int; JobRole: str; JobSatisfaction: int; MaritalStatus: str; MonthlyIncome: int; MonthlyRate: int; NumCompaniesWorked: int; OverTime: str; PercentSalaryHike: int; RelationshipSatisfaction: int; StockOptionLevel: int; TotalWorkingYears: int; TrainingTimesLastYear: int; WorkLifeBalance: int; YearsAtCompany: int; YearsInCurrentRole: int; YearsSinceLastPromotion: int; YearsWithCurrManager: int

PromotionData = PerformanceData

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


@app.post("/predict")
def predict(employee: EmployeeData):
    result = predict_attrition(employee.model_dump())

    return result


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
    return predict_promotion(employee.model_dump())
