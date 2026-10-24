import { ChangeDetectorRef, Component } from '@angular/core';
import { finalize } from 'rxjs';

import { Employee } from '../../models/employee';
import { SalaryPredictionResponse } from '../../models/salary-prediction';
import { SalaryPredictionService } from '../../services/salary-prediction';
import {
  SalaryPredictionFeatures,
  toSalaryPredictionFeatures
} from '../../services/prediction-features';
import { EmployeePicker } from '../employee-picker/employee-picker';

@Component({
  selector: 'app-salary-prediction',
  standalone: true,
  imports: [EmployeePicker],
  templateUrl: './salary-prediction.html',
  styleUrl: '../attrition-prediction/attrition-prediction.scss'
})
export class SalaryPrediction {
  selectedEmployee: Employee | null = null;
  prediction: SalaryPredictionResponse | null = null;
  loading = false;
  predictionLoading = false;
  errorMessage = '';
  employeeErrorMessage = '';
  employeesLoading = false;
  modelUnavailable = false;
  missingFields: string[] = [];

  constructor(
    private readonly salaryService: SalaryPredictionService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  onEmployeeSelected(employee: Employee | null): void {
    this.selectedEmployee = employee;
    this.clearPrediction();
  }

  clearPrediction(): void {
    this.prediction = null;
    this.errorMessage = '';
    this.modelUnavailable = false;
    this.missingFields = [];
  }

  predict(): void {
    if (this.loading) return;
    const employee = this.selectedEmployee;
    if (!employee) {
      this.errorMessage = 'Select an employee first.';
      return;
    }

    let features: SalaryPredictionFeatures;
    try {
      features = toSalaryPredictionFeatures(employee);
    } catch (error: unknown) {
      this.errorMessage = error instanceof Error
        ? error.message
        : 'Employee data is incomplete or invalid.';
      return;
    }

    this.loading = true;
    this.predictionLoading = true;
    this.clearPrediction();
    this.salaryService.predict(features)
      .pipe(finalize(() => {
        this.loading = false;
        this.predictionLoading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: result => {
          if (!result || !Number.isFinite(result.estimated_monthly_income)) {
            this.errorMessage = 'The model returned an invalid estimate. No salary estimate is available.';
          } else {
            this.prediction = result;
          }
          this.cdr.markForCheck();
        },
        error: (error: unknown) => {
          this.modelUnavailable = typeof error === 'object' && error !== null
            && 'status' in error && error.status === 503;
          this.errorMessage = this.modelUnavailable
            ? 'The salary model is currently unavailable. No estimate was returned.'
            : 'Salary estimate failed. Check that the backend and salary model are available.';
          this.cdr.markForCheck();
        }
      });
  }

  metricEntries(metrics: SalaryPredictionResponse['metrics']): Array<{ name: string; value: string }> {
    return Object.entries(metrics ?? {})
      .map(([name, value]) => ({ name, value: typeof value === 'object' ? JSON.stringify(value) : String(value) }));
  }
}
