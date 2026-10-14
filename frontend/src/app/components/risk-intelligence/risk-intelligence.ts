import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import { RiskIntelligenceService } from '../../services/risk-intelligence';
import { RiskSummary } from '../../models/risk-summary';

@Component({
  selector: 'app-risk-intelligence',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './risk-intelligence.html',
  styleUrl: './risk-intelligence.scss'
})
export class RiskIntelligence implements OnInit {
  employees: Employee[] = [];
  selectedEmployeeId: number | null = null;
  summary: RiskSummary | null = null;
  employeesLoading = true;
  summaryLoading = false;
  employeeErrorMessage = '';
  errorMessage = '';
  invalidResponse = false;

  constructor(
    private readonly employeesApi: EmployeeService,
    private readonly riskApi: RiskIntelligenceService
  ) {}

  ngOnInit(): void { this.loadEmployees(); }

  loadEmployees(): void {
    this.employeesLoading = true;
    this.employeeErrorMessage = '';
    this.errorMessage = '';
    this.summary = null;
    this.employeesApi.getEmployees().subscribe({
      next: response => {
        if (!Array.isArray(response)) {
          this.employees = [];
          this.selectedEmployeeId = null;
          this.employeeErrorMessage = 'The employee list returned an invalid response. Please try again.';
        } else {
          this.employees = response.filter(employee => this.isEmployeeWithId(employee));
          this.selectedEmployeeId = this.employees[0]?.id ?? null;
        }
        this.employeesLoading = false;
      },
      error: () => {
        this.employeesLoading = false;
        this.employeeErrorMessage = 'Unable to load employees for risk review.';
      }
    });
  }

  load(): void {
    if (this.summaryLoading || this.employeesLoading) return;
    const employee = this.employees.find(item => item.id === this.selectedEmployeeId);
    this.summary = null;
    this.errorMessage = '';
    this.invalidResponse = false;
    if (!employee || employee.id === undefined) {
      this.errorMessage = 'Select an employee first.';
      return;
    }

    this.summaryLoading = true;
    this.riskApi.getSummary(employee.id).subscribe({
      next: response => {
        if (!this.isValidSummary(response, employee.id!)) {
          this.invalidResponse = true;
          this.errorMessage = 'The risk service returned an invalid summary. No estimate is available.';
        } else {
          this.summary = response;
        }
        this.summaryLoading = false;
      },
      error: () => {
        this.errorMessage = 'Risk summary unavailable. Check the backend and ML services, then try again.';
        this.summaryLoading = false;
      }
    });
  }

  riskBand(value: number): string {
    if (value >= 0.67) return 'Higher risk';
    if (value >= 0.34) return 'Moderate risk';
    return 'Lower risk';
  }

  riskInterpretation(value: number): string {
    if (value >= 0.67) return 'Prioritize contextual review and supportive follow-up.';
    if (value >= 0.34) return 'Consider this a prompt for additional context, not a conclusion.';
    return 'No elevated signal in this composite; continue normal human review.';
  }

  private isEmployeeWithId(employee: Employee): boolean {
    return !!employee && Number.isInteger(employee.id) && employee.id! >= 0;
  }

  private isValidSummary(summary: RiskSummary, employeeId: number): boolean {
    if (!summary || typeof summary !== 'object' || summary.employeeId !== employeeId) return false;
    const scores = [summary.attritionRisk, summary.performanceRisk, summary.promotionReadiness, summary.overallRisk];
    return scores.every(value => Number.isFinite(value) && value >= 0 && value <= 1)
      && typeof summary.promotionTargetType === 'string'
      && !!summary.modelVersions && typeof summary.modelVersions === 'object' && !Array.isArray(summary.modelVersions)
      && Object.values(summary.modelVersions).every(value => typeof value === 'string')
      && Array.isArray(summary.limitations)
      && summary.limitations.every(value => typeof value === 'string' && value.trim().length > 0);
  }
}
