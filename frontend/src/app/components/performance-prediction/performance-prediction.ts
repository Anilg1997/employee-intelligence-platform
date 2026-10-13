import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import { PerformancePredictionService } from '../../services/performance-prediction';
import { PerformancePredictionResponse } from '../../models/performance-prediction';
@Component({ selector: 'app-performance-prediction', standalone: true, imports: [FormsModule, CommonModule], templateUrl: './performance-prediction.html', styleUrl: '../attrition-prediction/attrition-prediction.scss' })
export class PerformancePrediction implements OnInit {
  employees: Employee[] = [];
  selectedEmployeeId: number | null = null;
  prediction: PerformancePredictionResponse | null = null;
  employeesLoading = true;
  predictionLoading = false;
  employeeErrorMessage = '';
  errorMessage = '';
  modelUnavailable = false;
  missingFields: string[] = [];

  constructor(private readonly employeeService: EmployeeService, private readonly performanceService: PerformancePredictionService) {}

  ngOnInit(): void { this.loadEmployees(); }

  loadEmployees(): void {
    this.employeesLoading = true;
    this.employeeErrorMessage = '';
    this.employeeService.getEmployees().subscribe({
      next: employees => {
        this.employees = employees.filter(employee => employee.id !== undefined);
        this.selectedEmployeeId = this.employees[0]?.id ?? null;
        this.employeesLoading = false;
      },
      error: () => { this.employeesLoading = false; this.employeeErrorMessage = 'Unable to load employees for performance prediction.'; }
    });
  }

  clearPrediction(): void { this.prediction = null; this.errorMessage = ''; this.modelUnavailable = false; this.missingFields = []; }

  predict(): void {
    if (this.predictionLoading || this.employeesLoading) return;
    this.clearPrediction();
    const employee = this.employees.find(item => item.id === this.selectedEmployeeId);
    if (!employee) { this.errorMessage = 'Select an employee first.'; return; }
    const request = this.toRequest(employee);
    if (!request) { this.errorMessage = 'This employee does not have all fields required by the performance model.'; return; }
    this.predictionLoading = true;
    this.performanceService.predict(request).subscribe({
      next: result => {
        if (!this.isValidResult(result)) this.errorMessage = 'The performance model returned incomplete or invalid data. No rating estimate is available.';
        else this.prediction = result;
        this.predictionLoading = false;
      },
      error: error => {
        this.modelUnavailable = error?.status === 503;
        this.errorMessage = this.modelUnavailable
          ? 'The performance model is currently unavailable. No rating estimate was returned.'
          : 'Performance prediction failed. Check that the backend services are running and the employee data is valid.';
        this.predictionLoading = false;
      }
    });
  }

  probabilityEntries(probabilities: Record<string, number>): Array<{ label: string; value: number; percent: string }> {
    return Object.entries(probabilities ?? {}).filter(([, value]) => Number.isFinite(value) && value >= 0)
      .sort(([a], [b]) => Number(a) - Number(b)).map(([label, value]) => ({ label, value, percent: `${(value * 100).toFixed(1)}%` }));
  }

  metricEntries(metrics: PerformancePredictionResponse['metrics']): Array<{ name: string; value: string }> {
    return Object.entries(metrics ?? {}).map(([name, value]) => ({ name, value: typeof value === 'object' ? JSON.stringify(value) : String(value) }));
  }

  private isValidResult(result: PerformancePredictionResponse | null | undefined): result is PerformancePredictionResponse {
    return !!result && Number.isFinite(result.performance_prediction) && Number.isFinite(result.performance_probability)
      && result.performance_probability >= 0 && result.performance_probability <= 1
      && !!result.class_probabilities && Object.keys(result.class_probabilities).length > 0
      && Object.values(result.class_probabilities).every(value => Number.isFinite(value) && value >= 0)
      && typeof result.model_version === 'string' && typeof result.algorithm === 'string' && Array.isArray(result.limitations);
  }

  private toRequest(employee: Employee): Record<string, string | number> | null {
    const values: Record<string, string | number | undefined> = {
      Age: employee.age, BusinessTravel: employee.businessTravel, DailyRate: employee.dailyRate, Department: employee.department,
      DistanceFromHome: employee.distanceFromHome, Education: employee.education, EducationField: employee.educationField,
      EnvironmentSatisfaction: employee.environmentSatisfaction, Gender: employee.gender, HourlyRate: employee.hourlyRate,
      JobInvolvement: employee.jobInvolvement, JobLevel: employee.jobLevel, JobRole: employee.jobRole, JobSatisfaction: employee.jobSatisfaction,
      MaritalStatus: employee.maritalStatus, MonthlyIncome: employee.monthlyIncome, MonthlyRate: employee.monthlyRate,
      NumCompaniesWorked: employee.numCompaniesWorked, OverTime: employee.overTime, PercentSalaryHike: employee.percentSalaryHike,
      RelationshipSatisfaction: employee.relationshipSatisfaction, StockOptionLevel: employee.stockOptionLevel, TotalWorkingYears: employee.totalWorkingYears,
      TrainingTimesLastYear: employee.trainingTimesLastYear, WorkLifeBalance: employee.workLifeBalance, YearsAtCompany: employee.yearsAtCompany,
      YearsInCurrentRole: employee.yearsInCurrentRole, YearsSinceLastPromotion: employee.yearsSinceLastPromotion, YearsWithCurrManager: employee.yearsWithCurrManager
    };
    this.missingFields = Object.entries(values).filter(([, value]) => value === undefined || value === null || value === '' || (typeof value === 'number' && !Number.isFinite(value))).map(([field]) => field);
    return this.missingFields.length ? null : values as Record<string, string | number>;
  }
}
