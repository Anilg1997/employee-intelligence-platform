import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss'
})
export class EmployeeList implements OnInit {

  employees: Employee[] = [];

  loading = false;
  errorMessage = '';

  editingId: number | null = null;
  editingEmployee: Employee | null = null;
  expandedId: number | null = null;

  constructor(private employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.loading = true;
    this.errorMessage = '';

    this.employeeService.getEmployees().subscribe({
      next: (employees: Employee[]) => {
        this.employees = employees;
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Failed to load employees:', error);
        this.errorMessage = 'Failed to load employees.';
        this.loading = false;
      }
    });
  }

  startEdit(employee: Employee): void {
    this.editingId = employee.id ?? null;
    this.editingEmployee = { ...employee };
  }

  cancelEdit(): void {
    this.editingId = null;
    this.editingEmployee = null;
  }

  toggleDetails(id: number): void {
    this.expandedId = this.expandedId === id ? null : id;
  }

  saveEdit(): void {

    if (this.editingId === null || this.editingEmployee === null) {
      return;
    }

    const id = this.editingId;
    const employeeToUpdate: Employee = {
      ...this.editingEmployee
    };

    this.employeeService.updateEmployee(id, employeeToUpdate).subscribe({
      next: (updatedEmployee: Employee) => {

        this.employees = this.employees.map(employee =>
          employee.id === id
            ? updatedEmployee
            : employee
        );

        // Exit edit mode immediately after successful update.
        this.editingId = null;
        this.editingEmployee = null;
      },

      error: (error: unknown) => {
        console.error('Failed to update employee:', error);
        this.errorMessage = 'Failed to update employee.';
      }
    });
  }

  deleteEmployee(id: number): void {

    const confirmed = window.confirm(
      'Are you sure you want to delete this employee?'
    );

    if (!confirmed) {
      return;
    }

    this.employeeService.deleteEmployee(id).subscribe({
      next: () => {
        this.employees = this.employees.filter(
          employee => employee.id !== id
        );
      },

      error: (error: unknown) => {
        console.error('Failed to delete employee:', error);
        this.errorMessage = 'Failed to delete employee.';
      }
    });
  }
}