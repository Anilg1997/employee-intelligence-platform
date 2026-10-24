
import {
  Component,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';

import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import {
  AttritionMlService,
  AttritionFeatures,
  AttritionPredictionResponse
} from '../../services/attrition-ml.service';
import { toAttritionPredictionFeatures } from '../../services/prediction-features';

@Component({
  selector: 'app-attrition-prediction',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './attrition-prediction.html',
  styleUrl: './attrition-prediction.scss'
})
export class AttritionPrediction implements OnInit {
  employees: Employee[] = [];
  selectedEmployeeId: number | null = null;
  prediction: AttritionPredictionResponse | null = null;

  loading = false;
  loadingEmployees = false;
  errorMessage = '';
  modelStatus: 'checking' | 'ready' | 'unavailable' = 'checking';

  constructor(
    private readonly employeeService: EmployeeService,
    private readonly attritionMlService: AttritionMlService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
    this.checkModelReadiness();
  }

  private loadEmployees(): void {
    this.loadingEmployees = true;
    this.errorMessage = '';

    this.employeeService.getEmployees()
      .pipe(
        finalize(() => {
          this.loadingEmployees = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (employees) => {
          this.employees = employees.filter(
            employee => employee.id !== undefined
          );

          this.selectedEmployeeId = this.employees[0]?.id ?? null;

          if (this.employees.length === 0) {
            this.errorMessage =
              'No employees are available for prediction.';
          }

          this.cdr.detectChanges();
        },
        error: () => {
          this.errorMessage =
            'Unable to load employees. Check that Spring Boot is running.';
          this.cdr.detectChanges();
        }
      });
  }

  private checkModelReadiness(): void {
    this.attritionMlService.checkReadiness().subscribe({
      next: (readiness) => {
        this.modelStatus = this.hasReadyAttritionModel(readiness)
          ? 'ready'
          : 'unavailable';
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        const body = this.isRecord(error) ? error['error'] : undefined;
        const detail = this.isRecord(body) ? body['detail'] : undefined;
        this.modelStatus = this.hasReadyAttritionModel(detail)
          ? 'ready'
          : 'unavailable';
        this.cdr.detectChanges();
      }
    });
  }

  predict(): void {
    if (this.loading) {
      return;
    }

    const employee = this.employees.find(
      item => item.id === this.selectedEmployeeId
    );

    if (!employee) {
      this.errorMessage = 'Select an employee first.';
      this.cdr.detectChanges();
      return;
    }

    let request: AttritionFeatures;

    try {
      request = toAttritionPredictionFeatures(employee);
    } catch (error: unknown) {
      this.prediction = null;

      this.errorMessage =
        error instanceof Error
          ? error.message
          : 'Employee data is incomplete or invalid.';

      this.cdr.detectChanges();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.prediction = null;

    this.cdr.detectChanges();

    this.attritionMlService.predict(request)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (result: AttritionPredictionResponse) => {
          if (
            !result ||
            !['Yes', 'No'].includes(result.attrition_prediction) ||
            typeof result.attrition_probability !== 'number' ||
            !Number.isFinite(result.attrition_probability) ||
            result.attrition_probability < 0 ||
            result.attrition_probability > 1
          ) {
            this.prediction = null;
            this.errorMessage =
              'The ML API returned an invalid prediction response.';

            this.cdr.detectChanges();
            return;
          }

          this.prediction = result;
          this.errorMessage = '';

          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          this.prediction = null;
          const status = this.httpStatus(error);

          if (status === 0) {
            this.errorMessage =
              'Cannot connect to the ML service. Check that it is running and its URL is configured correctly.';
          } else if (status === 422) {
            this.errorMessage =
              'The ML service rejected the employee data. Verify the source record against the model requirements.';
          } else {
            this.errorMessage =
              `Prediction failed${status ? ` (HTTP ${status})` : ''}.`;
          }

          this.cdr.detectChanges();
        }
      });
  }

  private httpStatus(error: unknown): number | undefined {
    if (typeof error !== 'object' || error === null || !('status' in error)) {
      return undefined;
    }

    const status = error.status;
    return typeof status === 'number' ? status : undefined;
  }

  private hasReadyAttritionModel(value: unknown): boolean {
    if (!this.isRecord(value)) {
      return false;
    }

    const models = value['models'];
    return this.isRecord(models) && models['attrition'] === 'ready';
  }

  private isRecord(value: unknown): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null;
  }
}
