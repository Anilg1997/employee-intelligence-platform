import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';

import { Employee } from '../../models/employee';
import { MlPredictionRequest } from '../../models/ml-prediction-request';
import { MlPredictionResponse } from '../../models/ml-prediction-response';
import { EmployeeService } from '../../services/employee';
import { MlPredictionService } from '../../services/ml-prediction';

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
  employeesLoading = true;
  predictionLoading = false;
  errorMessage = '';
  employeeErrorMessage = '';
  modelUnavailable = false;
  missingFields: string[] = [];
  modelStatus: 'checking' | 'ready' | 'unavailable' = 'checking';

  constructor(
    private readonly employeeService: EmployeeService,
    private readonly mlPredictionService: MlPredictionService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.employeesLoading = true;
    this.employeeErrorMessage = '';
    this.employeeService.getEmployees().pipe(
      finalize(() => {
        this.employeesLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: employees => {
        this.employees = employees.filter(employee => Number.isInteger(employee.id));
        this.selectedEmployeeId = this.employees[0]?.id ?? null;
      },
      error: () => {
        this.employeeErrorMessage = 'Unable to load employees for prediction.';
      }
    });
  }

  clearPrediction(): void {
    this.prediction = null;
    this.errorMessage = '';
    this.modelUnavailable = false;
    this.missingFields = [];
  }

  predict(): void {
    if (this.predictionLoading || this.employeesLoading) return;
    this.clearPrediction();
    const employee = this.employees.find(item => item.id === this.selectedEmployeeId);
    if (!employee) {
      this.errorMessage = 'Select an employee first.';
      return;
    }
    const request = this.toPredictionRequest(employee);
    if (!request) {
      this.errorMessage = 'This employee does not have all fields required by the attrition model.';
      return;
    }

    this.predictionLoading = true;
    this.errorMessage = '';
    this.mlPredictionService.predict(request).pipe(finalize(() => {
      this.predictionLoading = false;
      this.cdr.detectChanges();
    })).subscribe({
      next: result => {
        if (!this.isValidResult(result)) {
          this.errorMessage = 'The model returned an invalid prediction. No estimate is available. Please try again.';
          this.modelStatus = 'unavailable';
        } else {
          this.prediction = result;
          this.modelStatus = 'ready';
        }
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        this.modelUnavailable = typeof error === 'object' && error !== null
          && 'status' in error && error.status === 503;
        this.modelStatus = this.modelUnavailable ? 'unavailable' : 'checking';
        this.errorMessage = this.modelUnavailable
          ? 'The attrition model is currently unavailable. No prediction was returned.'
          : 'Prediction failed. Check that the backend services are running and the employee data is valid.';
        this.cdr.detectChanges();
      }
    });
  }

  private isValidResult(result: MlPredictionResponse | null | undefined): result is MlPredictionResponse {
    return !!result && ['Yes', 'No'].includes(result.attrition_prediction)
      && Number.isFinite(result.attrition_probability)
      && result.attrition_probability >= 0 && result.attrition_probability <= 1
      && (result.decision_threshold === undefined
        || (Number.isFinite(result.decision_threshold) && result.decision_threshold >= 0 && result.decision_threshold <= 1))
      && (result.limitations === undefined || Array.isArray(result.limitations));
  }

  private toPredictionRequest(employee: Employee): MlPredictionRequest | null {
    const values: Record<string, string | number | null | undefined> = {
      Age: employee.age,
      BusinessTravel: employee.businessTravel,
      DailyRate: employee.dailyRate,
      Department: employee.department,
      DistanceFromHome: employee.distanceFromHome,
      Education: employee.education,
      EducationField: employee.educationField,
      EnvironmentSatisfaction: employee.environmentSatisfaction,
      Gender: employee.gender,
      HourlyRate: employee.hourlyRate,
      JobInvolvement: employee.jobInvolvement,
      JobLevel: employee.jobLevel,
      JobRole: employee.jobRole,
      JobSatisfaction: employee.jobSatisfaction,
      MaritalStatus: employee.maritalStatus,
      MonthlyIncome: employee.monthlyIncome,
      MonthlyRate: employee.monthlyRate,
      NumCompaniesWorked: employee.numCompaniesWorked,
      OverTime: employee.overTime,
      PercentSalaryHike: employee.percentSalaryHike,
      PerformanceRating: employee.performanceRating,
      RelationshipSatisfaction: employee.relationshipSatisfaction,
      StockOptionLevel: employee.stockOptionLevel,
      TotalWorkingYears: employee.totalWorkingYears,
      TrainingTimesLastYear: employee.trainingTimesLastYear,
      WorkLifeBalance: employee.workLifeBalance,
      YearsAtCompany: employee.yearsAtCompany,
      YearsInCurrentRole: employee.yearsInCurrentRole,
      YearsSinceLastPromotion: employee.yearsSinceLastPromotion,
      YearsWithCurrManager: employee.yearsWithCurrManager
    };
    this.missingFields = Object.entries(values)
      .filter(([, value]) => value === undefined || value === null || value === ''
        || (typeof value === 'number' && !Number.isFinite(value)))
      .map(([field]) => field);
    return this.missingFields.length ? null : values as unknown as MlPredictionRequest;
  }
}
