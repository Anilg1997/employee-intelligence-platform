import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs';

import { Employee } from '../../models/employee';
import { RiskSummary } from '../../models/risk-summary';
import { RiskIntelligenceService } from '../../services/risk-intelligence';
import { EmployeePicker } from '../employee-picker/employee-picker';

@Component({
  selector: 'app-risk-intelligence',
  standalone: true,
  imports: [CommonModule, EmployeePicker],
  templateUrl: './risk-intelligence.html',
  styleUrl: './risk-intelligence.scss'
})
export class RiskIntelligence {
  selectedEmployee: Employee | null = null;
  summary: RiskSummary | null = null;
  loading = false;
  summaryLoading = false;
  errorMessage = '';
  invalidResponse = false;

  constructor(
    private readonly riskApi: RiskIntelligenceService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  onEmployeeSelected(employee: Employee | null): void {
    this.selectedEmployee = employee;
    this.summary = null;
    this.errorMessage = '';
    this.invalidResponse = false;
  }

  load(): void {
    if (this.loading || this.summaryLoading) return;
    const employeeId = this.selectedEmployee?.id;
    if (!employeeId || !Number.isInteger(employeeId) || employeeId <= 0) {
      this.errorMessage = 'Select an employee first.';
      return;
    }

    this.loading = true;
    this.summaryLoading = true;
    this.errorMessage = '';
    this.summary = null;
    this.invalidResponse = false;
    this.riskApi.getSummary(employeeId)
      .pipe(finalize(() => {
        this.loading = false;
        this.summaryLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: response => {
          if (!this.isValidSummary(response, employeeId)) {
            this.invalidResponse = true;
            this.errorMessage = 'The risk service returned an invalid summary. No estimate is available.';
          } else {
            this.summary = response;
          }
          this.cdr.markForCheck();
        },
        error: () => {
          this.errorMessage = 'Risk summary is unavailable. Check the backend and prediction services, then retry.';
          this.cdr.markForCheck();
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

  private isValidSummary(summary: RiskSummary, employeeId: number): boolean {
    if (!summary || typeof summary !== 'object' || summary.employeeId !== employeeId) return false;
    const scores = [
      summary.attritionRisk,
      summary.performanceRisk,
      summary.promotionReadiness,
      summary.overallRisk
    ];
    return scores.every(value => Number.isFinite(value) && value >= 0 && value <= 1)
      && typeof summary.promotionTargetType === 'string'
      && !!summary.modelVersions && typeof summary.modelVersions === 'object' && !Array.isArray(summary.modelVersions)
      && Object.values(summary.modelVersions).every(value => typeof value === 'string')
      && Array.isArray(summary.limitations)
      && summary.limitations.every(value => typeof value === 'string' && value.trim().length > 0);
  }
}
