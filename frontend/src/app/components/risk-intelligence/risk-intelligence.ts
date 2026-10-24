import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs';
import { Employee } from '../../models/employee';
import { RiskIntelligenceService } from '../../services/risk-intelligence';
import { RiskSummary } from '../../models/risk-summary';
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
  errorMessage = '';

  constructor(
    private readonly riskApi: RiskIntelligenceService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  onEmployeeSelected(employee: Employee | null): void {
    this.selectedEmployee = employee;
    this.summary = null;
    this.errorMessage = '';
  }

  load(): void {
    if (this.loading) {
      return;
    }
    if (!this.selectedEmployee?.id) {
      this.errorMessage = 'Select an employee first.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.summary = null;
    this.riskApi.getSummary(this.selectedEmployee.id)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: summary => this.summary = summary,
        error: () => {
          this.errorMessage = 'Risk summary is unavailable. Check the backend and prediction services, then retry.';
        }
      });
  }
}
