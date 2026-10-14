import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import { PromotionPredictionService } from '../../services/promotion-prediction';
import { PromotionPredictionResponse } from '../../models/promotion-prediction';

@Component({ selector: 'app-promotion-prediction', standalone: true, imports: [FormsModule, CommonModule], templateUrl: './promotion-prediction.html', styleUrl: '../attrition-prediction/attrition-prediction.scss' })
export class PromotionPrediction implements OnInit {
  employees: Employee[] = [];
  selectedEmployeeId: number | null = null;
  prediction: PromotionPredictionResponse | null = null;
  employeesLoading = true;
  predictionLoading = false;
  employeeErrorMessage = '';
  errorMessage = '';
  modelUnavailable = false;
  missingFields: string[] = [];

  constructor(private readonly employeeService: EmployeeService, private readonly promotionService: PromotionPredictionService) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.employeesLoading = true;
    this.employeeErrorMessage = '';
    this.employeeService.getEmployees().subscribe({
      next: employees => {
        this.employees = employees.filter(employee => employee.id !== undefined);
        this.selectedEmployeeId = this.employees[0]?.id ?? null;
        this.employeesLoading = false;
      },
      error: () => { this.employeesLoading = false; this.employeeErrorMessage = 'Unable to load employees for promotion-readiness prediction.'; }
    });
  }

  clearPrediction(): void { this.prediction = null; this.errorMessage = ''; this.modelUnavailable = false; this.missingFields = []; }

  predict(): void {
    if (this.predictionLoading || this.employeesLoading) return;
    this.clearPrediction();
    const employee = this.employees.find(e => e.id === this.selectedEmployeeId);
    if (!employee) { this.errorMessage = 'Select an employee first.'; return; }
    const request = this.toRequest(employee);
    if (!request) { this.errorMessage = 'This employee does not have all fields required by the promotion model.'; return; }
    this.predictionLoading = true;
    this.promotionService.predict(request).subscribe({
      next: result => {
        if (!this.isValidResult(result)) this.errorMessage = 'The promotion model returned incomplete or invalid data. No readiness estimate is available.';
        else this.prediction = result;
        this.predictionLoading = false;
      },
      error: error => {
        this.modelUnavailable = error?.status === 503;
        this.errorMessage = this.modelUnavailable
          ? 'The promotion model is currently unavailable. No readiness estimate was returned.'
          : 'Promotion prediction failed. Check that the backend services are running and the employee data is valid.';
        this.predictionLoading = false;
      }
    });
  }

  ruleEntries(checks: Record<string, boolean> | undefined): Array<{ name: string; met: boolean }> {
    return Object.entries(checks ?? {}).map(([name, met]) => ({ name, met }));
  }

  private isValidResult(result: PromotionPredictionResponse | null | undefined): result is PromotionPredictionResponse {
    return !!result && typeof result.promotion_prediction === 'string' && result.promotion_prediction.length > 0
      && Number.isFinite(result.promotion_probability) && result.promotion_probability >= 0 && result.promotion_probability <= 1
      && Number.isFinite(result.rule_score) && result.rule_score >= 0 && result.rule_score <= 1
      && !!result.rule_checks && Object.keys(result.rule_checks).length > 0
      && Object.values(result.rule_checks).every(value => typeof value === 'boolean')
      && typeof result.model_version === 'string' && typeof result.algorithm === 'string'
      && Number.isFinite(result.decision_threshold) && result.decision_threshold >= 0 && result.decision_threshold <= 1
      && typeof result.target_type === 'string' && Array.isArray(result.limitations);
  }

  private toRequest(e: Employee): Record<string, string | number> | null {
    const values: Record<string, string | number | undefined> = { Age: e.age, BusinessTravel: e.businessTravel, DailyRate: e.dailyRate, Department: e.department, DistanceFromHome: e.distanceFromHome, Education: e.education, EducationField: e.educationField, EnvironmentSatisfaction: e.environmentSatisfaction, Gender: e.gender, HourlyRate: e.hourlyRate, JobInvolvement: e.jobInvolvement, JobLevel: e.jobLevel, JobRole: e.jobRole, JobSatisfaction: e.jobSatisfaction, MaritalStatus: e.maritalStatus, MonthlyIncome: e.monthlyIncome, MonthlyRate: e.monthlyRate, NumCompaniesWorked: e.numCompaniesWorked, OverTime: e.overTime, PercentSalaryHike: e.percentSalaryHike, PerformanceRating: e.performanceRating, RelationshipSatisfaction: e.relationshipSatisfaction, StockOptionLevel: e.stockOptionLevel, TotalWorkingYears: e.totalWorkingYears, TrainingTimesLastYear: e.trainingTimesLastYear, WorkLifeBalance: e.workLifeBalance, YearsAtCompany: e.yearsAtCompany, YearsInCurrentRole: e.yearsInCurrentRole, YearsSinceLastPromotion: e.yearsSinceLastPromotion, YearsWithCurrManager: e.yearsWithCurrManager };
    this.missingFields = Object.entries(values).filter(([, value]) => value === undefined || value === null || value === '' || (typeof value === 'number' && !Number.isFinite(value))).map(([field]) => field);
    return this.missingFields.length ? null : values as Record<string, string | number>;
  }
}
