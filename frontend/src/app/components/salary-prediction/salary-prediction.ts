import { ChangeDetectorRef, Component } from '@angular/core';
import { Employee } from '../../models/employee';
import { SalaryPredictionService } from '../../services/salary-prediction';
import { SalaryPredictionResponse } from '../../models/salary-prediction';
import { finalize } from 'rxjs';
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
  errorMessage = '';

  constructor(
    private readonly salaryService: SalaryPredictionService,
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
    this.errorMessage = '';
    this.prediction = null;
    this.salaryService.predict(features)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: result => this.prediction = result,
        error: () => this.errorMessage = 'Salary estimate failed. Check that the backend and salary model are available.'
      });
  }
}
