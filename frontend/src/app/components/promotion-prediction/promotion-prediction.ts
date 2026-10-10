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
  loading = false;
  errorMessage = '';
  constructor(private employeeService: EmployeeService, private promotionService: PromotionPredictionService) {}
  ngOnInit(): void {
    this.employeeService.getEmployees().subscribe({
      next: employees => { this.employees = employees.filter(e => e.id !== undefined); this.selectedEmployeeId = this.employees[0]?.id ?? null; },
      error: () => this.errorMessage = 'Unable to load employees for promotion-readiness prediction.'
    });
  }
  predict(): void {
    const employee = this.employees.find(e => e.id === this.selectedEmployeeId);
    if (!employee) { this.errorMessage = 'Select an employee first.'; return; }
    this.loading = true; this.errorMessage = ''; this.prediction = null;
    this.promotionService.predict(this.toRequest(employee)).subscribe({
      next: result => { this.prediction = result; this.loading = false; },
      error: () => { this.errorMessage = 'Prediction failed. Check that both backend services are running.'; this.loading = false; }
    });
  }
  private toRequest(e: Employee): Record<string, string | number> {
    return { Age: e.age, BusinessTravel: e.businessTravel ?? 'Travel_Rarely', DailyRate: e.dailyRate ?? 500, Department: e.department, DistanceFromHome: e.distanceFromHome ?? 1, Education: e.education ?? 3, EducationField: e.educationField ?? 'Other', EnvironmentSatisfaction: e.environmentSatisfaction ?? 3, Gender: e.gender ?? 'Female', HourlyRate: e.hourlyRate ?? 60, JobInvolvement: e.jobInvolvement ?? 3, JobLevel: e.jobLevel ?? 1, JobRole: e.jobRole, JobSatisfaction: e.jobSatisfaction ?? 3, MaritalStatus: e.maritalStatus ?? 'Single', MonthlyIncome: e.monthlyIncome ?? 3000, MonthlyRate: e.monthlyRate ?? 14000, NumCompaniesWorked: e.numCompaniesWorked ?? 1, OverTime: e.overTime ?? 'No', PercentSalaryHike: e.percentSalaryHike ?? 15, PerformanceRating: e.performanceRating ?? 3, RelationshipSatisfaction: e.relationshipSatisfaction ?? 3, StockOptionLevel: e.stockOptionLevel ?? 0, TotalWorkingYears: e.totalWorkingYears ?? 5, TrainingTimesLastYear: e.trainingTimesLastYear ?? 3, WorkLifeBalance: e.workLifeBalance ?? 3, YearsAtCompany: e.yearsAtCompany ?? 1, YearsInCurrentRole: e.yearsInCurrentRole ?? 1, YearsSinceLastPromotion: e.yearsSinceLastPromotion ?? 0, YearsWithCurrManager: e.yearsWithCurrManager ?? 1 };
  }
}
