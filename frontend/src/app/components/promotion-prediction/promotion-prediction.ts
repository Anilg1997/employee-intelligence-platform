import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Employee } from '../../models/employee';
import { PromotionPredictionService } from '../../services/promotion-prediction';
import { PromotionPredictionResponse } from '../../models/promotion-prediction';
import { finalize } from 'rxjs';
import {
  AttritionPredictionFeatures,
  toPromotionPredictionFeatures
} from '../../services/prediction-features';
import { EmployeePicker } from '../employee-picker/employee-picker';

@Component({
  selector: 'app-promotion-prediction',
  standalone: true,
  imports: [FormsModule, CommonModule, EmployeePicker],
  templateUrl: './promotion-prediction.html',
  styleUrl: '../attrition-prediction/attrition-prediction.scss'
})
export class PromotionPrediction {
  selectedEmployee: Employee | null = null;
  prediction: PromotionPredictionResponse | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private readonly promotionService: PromotionPredictionService,
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

    let features: AttritionPredictionFeatures;
    try {
      features = toPromotionPredictionFeatures(employee);
    } catch (error: unknown) {
      this.errorMessage = error instanceof Error
        ? error.message
        : 'Employee data is incomplete or invalid.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.prediction = null;
    this.promotionService.predict(features)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: result => this.prediction = result,
        error: () => this.errorMessage = 'Promotion readiness evaluation failed. Check that the backend and model are available.'
      });
  }
}
