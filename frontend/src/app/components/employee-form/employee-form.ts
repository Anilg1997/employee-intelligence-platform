import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';

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
  errorMessage = '';

  constructor(private employeeService: EmployeeService) {}

  createEmployee(): void {

    this.errorMessage = '';

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

        this.employee = this.emptyEmployee();

        this.loading = false;
      },

      error: (error: unknown) => {
        console.error('Failed to create employee:', error);
        this.errorMessage = 'Failed to create employee.';
        this.loading = false;
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