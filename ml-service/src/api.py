from fastapi import FastAPI
from pydantic import BaseModel

from .predict import predict_attrition

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


@app.get("/")
def home():
    return {
        "message": "Employee Attrition Prediction API is running"
    }


@app.post("/predict")
def predict(employee: EmployeeData):
    result = predict_attrition(employee.model_dump())

    return result