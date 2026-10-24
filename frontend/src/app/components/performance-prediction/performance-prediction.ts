import { ChangeDetectorRef, Component } from '@angular/core';
import { Employee } from '../../models/employee';
import { PerformancePredictionService } from '../../services/performance-prediction';
import { PerformancePredictionResponse } from '../../models/performance-prediction';
import { finalize } from 'rxjs';
import {
  EmployeePredictionFeatures,
  toPerformancePredictionFeatures
} from '../../services/prediction-features';
import { EmployeePicker } from '../employee-picker/employee-picker';

@Component({
  selector: 'app-performance-prediction',
  standalone: true,
  imports: [EmployeePicker],
  templateUrl: './performance-prediction.html',
  styleUrl: '../attrition-prediction/attrition-prediction.scss'
})
export class PerformancePrediction {
  selectedEmployee: Employee | null = null;
  prediction: PerformancePredictionResponse | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private readonly performanceService: PerformancePredictionService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  onEmployeeSelected(employee: Employee | null): void {
    this.selectedEmployee = employee;
    this.prediction = null;
    this.errorMessage = '';
  }

  predict(): void {
    if (this.loading) {
      return;
    }

    const employee = this.selectedEmployee;
    if (!employee) {
      this.errorMessage = 'Select an employee first.';
      return;
    }

    let features: EmployeePredictionFeatures;
    try {
      features = toPerformancePredictionFeatures(employee);
    } catch (error: unknown) {
      this.errorMessage = error instanceof Error
        ? error.message
        : 'Employee data is incomplete or invalid.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.prediction = null;
    this.performanceService.predict(features)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: result => this.prediction = result,
        error: () => this.errorMessage = 'Performance prediction failed. Check that the backend and model are available.'
      });
  }
}
