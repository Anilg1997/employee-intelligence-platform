import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import {
  EmployeeAiService,
  EmployeeAiPrediction
} from '../../services/employee-ai.service';
import { RolePolicyService } from '../../services/role-policy.service';
import { toAttritionPredictionFeatures } from '../../services/prediction-features';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './employee-form.html',
  styleUrl: './employee-form.scss'
})
export class EmployeeForm {

  @Output() employeeCreated = new EventEmitter<Employee>();

  employee: Employee = {
    name: '',
    department: '',
    jobRole: '',
    age: null
  };

  loading = false;
  predictionLoading = false;

  errorMessage = '';
  predictionNotice = '';

  readonly departments = ['Human Resources', 'Research & Development', 'Sales'];

  prediction: EmployeeAiPrediction | null = null;

  constructor(
    private employeeService: EmployeeService,
    private employeeAiService: EmployeeAiService,
    readonly policy: RolePolicyService
  ) {}

  createEmployee(): void {

    this.errorMessage = '';
    this.predictionNotice = '';
    this.prediction = null;
    const age = this.employee.age;

    if (
      !this.employee.name?.trim() ||
      !this.employee.department?.trim() ||
      !this.employee.jobRole?.trim() ||
      typeof age !== 'number' ||
      !Number.isInteger(age) ||
      age < 18 ||
      age > 60
    ) {
      this.errorMessage = 'Enter a name, department, job role, and an age from 18 to 60.';
      return;
    }

    if (this.loading) {
      return;
    }

    this.loading = true;

    this.employeeService.createEmployee(this.employee).subscribe({
      next: (createdEmployee: Employee) => {

        this.employeeCreated.emit(createdEmployee);

        this.loading = false;

        if (createdEmployee.id) {
          try {
            toAttritionPredictionFeatures(createdEmployee);
            this.predictEmployee(createdEmployee.id);
          } catch (error: unknown) {
            this.predictionNotice = error instanceof Error
              ? `Employee created. Attrition prediction was skipped: ${error.message}`
              : 'Employee created. Attrition prediction was skipped because required model features are missing.';
          }
        }

        this.employee = this.emptyEmployee();
      },

      error: () => {
        this.errorMessage = 'Failed to create employee.';
        this.loading = false;
      }
    });
  }

  private predictEmployee(employeeId: number): void {

    this.predictionLoading = true;

    this.employeeAiService
      .predictEmployee(employeeId)
      .subscribe({
        next: (result: EmployeeAiPrediction) => {

          this.prediction = result;
          this.predictionLoading = false;

        },

        error: () => {
          this.predictionNotice =
            'Employee created, but the attrition model is currently unavailable.';

          this.predictionLoading = false;
        }
      });
  }

  private emptyEmployee(): Employee {
    return {
      name: '',
      department: '',
      jobRole: '',
      age: null
    };
  }
}
