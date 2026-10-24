import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, map } from 'rxjs';

import { Employee } from '../../models/employee';
import { PerformancePredictionResponse } from '../../models/performance-prediction';
import { EmployeeService } from '../../services/employee';
import { PerformancePredictionService } from '../../services/performance-prediction';
import {
  EmployeePredictionFeatures,
  toPerformancePredictionFeatures
} from '../../services/prediction-features';

@Component({
  selector: 'app-performance-prediction',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './performance-prediction.html',
  styleUrl: '../attrition-prediction/attrition-prediction.scss'
})
export class PerformancePrediction implements OnInit {
  employees: Employee[] = [];
  selectedEmployeeId: number | null = null;
  employeesLoading = true;
  predictionLoading = false;
  employeeErrorMessage = '';
  prediction: PerformancePredictionResponse | null = null;
  loading = false;
  errorMessage = '';
  modelUnavailable = false;
  missingFields: string[] = [];
  searchQuery = '';

  constructor(
    private readonly employeeService: EmployeeService,
    private readonly performanceService: PerformancePredictionService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  get selectedEmployee(): Employee | null {
    return this.employees.find(employee => employee.id === this.selectedEmployeeId) ?? null;
  }

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.employeesLoading = true;
    this.employeeErrorMessage = '';
    const searchEmployees = this.employeeService.searchEmployees;
    const result$ = typeof searchEmployees === 'function'
      ? searchEmployees.call(this.employeeService, {
          query: this.searchQuery,
          page: 0,
          size: 20,
          sortBy: 'name',
          sortDirection: 'ASC'
        }).pipe(map(page => page.content))
      : this.employeeService.getEmployees();

    result$.pipe(finalize(() => {
      this.employeesLoading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: employees => {
        this.employees = Array.isArray(employees)
          ? employees.filter(employee => Number.isInteger(employee.id))
          : [];
        this.selectedEmployeeId = this.employees[0]?.id ?? null;
      },
      error: () => {
        this.employees = [];
        this.selectedEmployeeId = null;
        this.employeeErrorMessage = 'Unable to load employees for performance prediction.';
      }
    });
  }

  clearPrediction(): void {
    this.prediction = null;
    this.errorMessage = '';
    this.modelUnavailable = false;
    this.missingFields = [];
  }

  predict(): void {
    if (this.predictionLoading || this.employeesLoading) return;
    this.clearPrediction();
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
      const field = this.errorMessage.match(/^([A-Za-z]+)/)?.[1];
      this.missingFields = field ? [field] : [];
      return;
    }

    this.predictionLoading = true;
    this.loading = true;
    this.performanceService.predict(features).pipe(finalize(() => {
      this.predictionLoading = false;
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: result => {
        if (!this.isValidResult(result)) {
          this.errorMessage = 'The performance model returned incomplete or invalid data. No rating estimate is available.';
        } else {
          this.prediction = result;
        }
      },
      error: (error: unknown) => {
        this.modelUnavailable = typeof error === 'object' && error !== null
          && 'status' in error && error.status === 503;
        this.errorMessage = this.modelUnavailable
          ? 'The performance model is currently unavailable. No rating estimate was returned.'
          : 'Performance prediction failed. Check that the backend and model are available.';
      }
    });
  }

  probabilityEntries(probabilities: Record<string, number>): Array<{ label: string; value: number; percent: string }> {
    return Object.entries(probabilities ?? {})
      .filter(([, value]) => Number.isFinite(value) && value >= 0)
      .sort(([first], [second]) => Number(first) - Number(second))
      .map(([label, value]) => ({ label, value, percent: `${(value * 100).toFixed(1)}%` }));
  }

  metricEntries(metrics: PerformancePredictionResponse['metrics']): Array<{ name: string; value: string }> {
    return Object.entries(metrics ?? {})
      .map(([name, value]) => ({ name, value: typeof value === 'object' ? JSON.stringify(value) : String(value) }));
  }

  private isValidResult(result: PerformancePredictionResponse | null | undefined): result is PerformancePredictionResponse {
    return !!result && Number.isFinite(result.performance_prediction)
      && Number.isFinite(result.performance_probability)
      && result.performance_probability >= 0 && result.performance_probability <= 1
      && !!result.class_probabilities && Object.keys(result.class_probabilities).length > 0
      && Object.values(result.class_probabilities).every(value => Number.isFinite(value) && value >= 0)
      && typeof result.model_version === 'string' && typeof result.algorithm === 'string'
      && Array.isArray(result.limitations);
  }
}
