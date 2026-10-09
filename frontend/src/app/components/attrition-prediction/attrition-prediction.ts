import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import { MlPredictionService } from '../../services/ml-prediction';
import { MlPredictionRequest } from '../../models/ml-prediction-request';
import { MlPredictionResponse } from '../../models/ml-prediction-response';

@Component({
  selector: 'app-attrition-prediction',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './attrition-prediction.html',
  styleUrl: './attrition-prediction.scss'
})
export class AttritionPrediction implements OnInit {

  employees: Employee[] = [];
  selectedEmployeeId: number | null = null;
  prediction: MlPredictionResponse | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private employeeService: EmployeeService,
    private mlPredictionService: MlPredictionService
  ) {}

  ngOnInit(): void {
    this.employeeService.getEmployees().subscribe({
      next: employees => {
        this.employees = employees.filter(employee => employee.id !== undefined);
        this.selectedEmployeeId = this.employees[0]?.id ?? null;
      },
      error: () => this.errorMessage = 'Unable to load employees for prediction.'
    });
  }

  predict(): void {
    const employee = this.employees.find(item => item.id === this.selectedEmployeeId);
    if (!employee) {
      this.errorMessage = 'Select an employee first.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.prediction = null;

    this.mlPredictionService.predict(this.toPredictionRequest(employee)).subscribe({
      next: result => {
        this.prediction = result;
        this.loading = false;
      },
      error: error => {
        console.error('Failed to predict attrition:', error);
        this.errorMessage = 'Prediction failed. Check that both backend services are running.';
        this.loading = false;
      }
    });
  }

  private toPredictionRequest(employee: Employee): MlPredictionRequest {
    return {
      Age: employee.age,
      BusinessTravel: employee.businessTravel ?? 'Travel_Rarely',
      DailyRate: employee.dailyRate ?? 500,
      Department: employee.department,
      DistanceFromHome: employee.distanceFromHome ?? 1,
      Education: employee.education ?? 3,
      EducationField: employee.educationField ?? 'Other',
      EnvironmentSatisfaction: employee.environmentSatisfaction ?? 3,
      Gender: employee.gender ?? 'Female',
      HourlyRate: employee.hourlyRate ?? 60,
      JobInvolvement: employee.jobInvolvement ?? 3,
      JobLevel: employee.jobLevel ?? 1,
      JobRole: employee.jobRole,
      JobSatisfaction: employee.jobSatisfaction ?? 3,
      MaritalStatus: employee.maritalStatus ?? 'Single',
      MonthlyIncome: employee.monthlyIncome ?? 3000,
      MonthlyRate: employee.monthlyRate ?? 14000,
      NumCompaniesWorked: employee.numCompaniesWorked ?? 1,
      OverTime: employee.overTime ?? 'No',
      PercentSalaryHike: employee.percentSalaryHike ?? 15,
      PerformanceRating: employee.performanceRating ?? 3,
      RelationshipSatisfaction: employee.relationshipSatisfaction ?? 3,
      StockOptionLevel: employee.stockOptionLevel ?? 0,
      TotalWorkingYears: employee.totalWorkingYears ?? 5,
      TrainingTimesLastYear: employee.trainingTimesLastYear ?? 3,
      WorkLifeBalance: employee.workLifeBalance ?? 3,
      YearsAtCompany: employee.yearsAtCompany ?? 1,
      YearsInCurrentRole: employee.yearsInCurrentRole ?? 1,
      YearsSinceLastPromotion: employee.yearsSinceLastPromotion ?? 0,
      YearsWithCurrManager: employee.yearsWithCurrManager ?? 1
    };
  }
}
