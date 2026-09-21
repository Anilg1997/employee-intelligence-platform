import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import {
  EmployeeAiService,
  EmployeeAiPrediction
} from '../../services/employee-ai.service';

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
    age: 18,
    attrition: 'No',
    businessTravel: 'Travel_Rarely',
    gender: 'Female',
    maritalStatus: 'Single',
    overTime: 'No',
    monthlyIncome: 0,
    totalWorkingYears: 0,
    yearsAtCompany: 0
  };

  loading = false;
  predictionLoading = false;

  errorMessage = '';

  prediction: EmployeeAiPrediction | null = null;

  constructor(
    private employeeService: EmployeeService,
    private employeeAiService: EmployeeAiService
  ) {}

  createEmployee(): void {

    this.errorMessage = '';
    this.prediction = null;

    if (
      !this.employee.name ||
      !this.employee.department ||
      !this.employee.jobRole ||
      this.employee.age < 18
    ) {
      this.errorMessage = 'Please fill all fields correctly.';
      return;
    }

    this.loading = true;

    this.employeeService.createEmployee(this.employee).subscribe({
      next: (createdEmployee: Employee) => {

        this.employeeCreated.emit(createdEmployee);

        this.loading = false;

        // Get AI prediction for the newly created employee
        if (createdEmployee.id) {
          this.predictEmployee(createdEmployee.id);
        }

        this.employee = this.emptyEmployee();
      },

      error: (error: unknown) => {
        console.error('Failed to create employee:', error);

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

        error: (error: unknown) => {

          console.error(
            'Failed to predict employee attrition:',
            error
          );

          this.errorMessage =
            'Employee created, but AI prediction failed.';

          this.predictionLoading = false;
        }
      });
  }

  private emptyEmployee(): Employee {
    return {
      name: '',
      department: '',
      jobRole: '',
      age: 18,
      attrition: 'No',
      businessTravel: 'Travel_Rarely',
      gender: 'Female',
      maritalStatus: 'Single',
      overTime: 'No',
      monthlyIncome: 0,
      totalWorkingYears: 0,
      yearsAtCompany: 0
    };
  }
}