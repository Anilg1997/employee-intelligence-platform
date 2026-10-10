import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';

import {
  EmployeeAiService,
  AttritionRiskResponse
} from '../../services/employee-ai.service';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss'
})
export class EmployeeList implements OnInit, OnChanges {

  // --------------------------------------------------
  // Refresh trigger from EmployeesPage
  // --------------------------------------------------

  @Input() refreshTrigger = 0;
  @Output() employeeChanged = new EventEmitter<void>();

  // --------------------------------------------------
  // Employee data
  // --------------------------------------------------

  employees: Employee[] = [];
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 20;
  searchQuery = '';
  selectedDepartment = '';
  sortBy = 'name';
  sortDirection: 'ASC' | 'DESC' = 'ASC';

  readonly departments = ['Human Resources', 'Research & Development', 'Sales'];
  readonly sortOptions = [
    { value: 'name', label: 'Name' },
    { value: 'department', label: 'Department' },
    { value: 'jobRole', label: 'Job role' },
    { value: 'age', label: 'Age' },
    { value: 'employeeNumber', label: 'Employee number' }
  ];

  // --------------------------------------------------
  // Loading and error state
  // --------------------------------------------------

  loading = false;
  errorMessage = '';

  // --------------------------------------------------
  // Edit state
  // --------------------------------------------------

  editingId: number | null = null;
  editingEmployee: Employee | null = null;

  // --------------------------------------------------
  // Employee details state
  // --------------------------------------------------

  expandedId: number | null = null;

  // --------------------------------------------------
  // AI prediction state
  // --------------------------------------------------

  predictionLoadingId: number | null = null;

riskAssessments: {
  [employeeId: number]: AttritionRiskResponse;
} = {};

predictionErrors: {
  [employeeId: number]: string;
} = {};

  // --------------------------------------------------
  // Constructor
  // --------------------------------------------------

  constructor(
    private employeeService: EmployeeService,
    private employeeAiService: EmployeeAiService
  ) {}

  // --------------------------------------------------
  // Angular lifecycle
  // --------------------------------------------------

  ngOnInit(): void {
    this.loadEmployees();
  }

  ngOnChanges(changes: SimpleChanges): void {

    if (
      changes['refreshTrigger'] &&
      !changes['refreshTrigger'].firstChange
    ) {
      this.loadEmployees();
    }
  }

  // --------------------------------------------------
  // Load employees
  // --------------------------------------------------

  loadEmployees(): void {

    this.loading = true;
    this.errorMessage = '';

    this.employeeService.searchEmployees({
      query: this.searchQuery,
      department: this.selectedDepartment,
      page: this.currentPage,
      size: this.pageSize,
      sortBy: this.sortBy,
      sortDirection: this.sortDirection
    }).subscribe({

      next: (response) => {

        this.employees = response.content;
        this.currentPage = response.page;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;

        this.loading = false;
      },

      error: (error: unknown) => {

        console.error(
          'Failed to load employees:',
          error
        );

        this.errorMessage =
          'Failed to load employees.';

        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadEmployees();
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage) {
      return;
    }
    this.currentPage = page;
    this.loadEmployees();
  }

  changePageSize(): void {
    this.currentPage = 0;
    this.loadEmployees();
  }

  // --------------------------------------------------
  // Start editing
  // --------------------------------------------------

  startEdit(employee: Employee): void {

    this.editingId = employee.id ?? null;

    this.editingEmployee = {
      ...employee
    };
  }

  // --------------------------------------------------
  // Cancel editing
  // --------------------------------------------------

  cancelEdit(): void {

    this.editingId = null;

    this.editingEmployee = null;
  }

  // --------------------------------------------------
  // Toggle employee details
  // --------------------------------------------------

  toggleDetails(id: number): void {

    this.expandedId =
      this.expandedId === id
        ? null
        : id;
  }

  // --------------------------------------------------
  // Save employee changes
  // --------------------------------------------------

  saveEdit(): void {

    if (
      this.editingId === null ||
      this.editingEmployee === null
    ) {
      return;
    }

    const id = this.editingId;

    const employeeToUpdate: Employee = {
      ...this.editingEmployee
    };

    this.employeeService
      .updateEmployee(id, employeeToUpdate)
      .subscribe({

        next: (updatedEmployee: Employee) => {

          this.employees =
            this.employees.map(employee =>
              employee.id === id
                ? updatedEmployee
                : employee
            );

          // Exit edit mode
          this.editingId = null;
          this.editingEmployee = null;
          this.employeeChanged.emit();
        },

        error: (error: unknown) => {

          console.error(
            'Failed to update employee:',
            error
          );

          this.errorMessage =
            'Failed to update employee.';
        }
      });
  }

  // --------------------------------------------------
  // Delete employee
  // --------------------------------------------------

  deleteEmployee(id: number): void {

    const confirmed = window.confirm(
      'Are you sure you want to delete this employee?'
    );

    if (!confirmed) {
      return;
    }

    this.employeeService
      .deleteEmployee(id)
      .subscribe({

        next: () => {

          // Remove employee from UI
          this.employees =
            this.employees.filter(
              employee => employee.id !== id
            );

          // Remove prediction data
          delete this.riskAssessments[id];

          delete this.predictionErrors[id];

          // Close details if this employee was expanded
          if (this.expandedId === id) {
            this.expandedId = null;
          }
          this.employeeChanged.emit();
        },

        error: (error: unknown) => {

          console.error(
            'Failed to delete employee:',
            error
          );

          this.errorMessage =
            'Failed to delete employee.';
        }
      });
  }

  // --------------------------------------------------
  // AI Attrition Prediction
  // --------------------------------------------------
predictAttrition(employeeId: number): void {

  this.predictionLoadingId = employeeId;

  delete this.predictionErrors[employeeId];

  this.employeeAiService
    .assessRisk(employeeId)
    .subscribe({

      next: (result: AttritionRiskResponse) => {

        this.riskAssessments[employeeId] = result;

        this.predictionLoadingId = null;
      },

      error: (error: unknown) => {

        console.error(
          'Failed to assess employee attrition risk:',
          error
        );

        this.predictionErrors[employeeId] =
          'AI risk assessment failed.';

        this.predictionLoadingId = null;
      }
    });
}

}
