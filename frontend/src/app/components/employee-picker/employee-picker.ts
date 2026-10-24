import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, of } from 'rxjs';
import { catchError, debounceTime, finalize, switchMap, takeUntil, timeout } from 'rxjs/operators';
import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';

@Component({
  selector: 'app-employee-picker',
  standalone: true,
  imports: [FormsModule],
  template: `
    <label for="employeeSearch">Find employee</label>
    <input
      id="employeeSearch"
      type="search"
      [(ngModel)]="query"
      (ngModelChange)="search($event)"
      placeholder="Search by name, department, job role, or employee number"
      autocomplete="off">
    @if (loading) {
      <p role="status">Loading employees...</p>
    } @else if (errorMessage) {
      <p class="error" role="alert">{{ errorMessage }}</p>
      <button type="button" (click)="load()">Retry</button>
    } @else if (!employees.length) {
      <p role="status">No employees match this search.</p>
    }
    <label for="predictionEmployee">Employee</label>
    <select
      id="predictionEmployee"
      [(ngModel)]="selectedEmployeeId"
      (ngModelChange)="select($event)"
      [disabled]="loading || !employees.length">
      <option [ngValue]="null">Select an employee</option>
      @for (employee of employees; track employee.id) {
        <option [ngValue]="employee.id">{{ employee.name }} · {{ employee.jobRole }}</option>
      }
    </select>
  `
})
export class EmployeePicker implements OnInit, OnDestroy {
  @Output() readonly employeeSelected = new EventEmitter<Employee | null>();

  employees: Employee[] = [];
  query = '';
  selectedEmployeeId: number | null = null;
  loading = false;
  errorMessage = '';

  private readonly searchTerms = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  constructor(private readonly employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.searchTerms.pipe(
      debounceTime(250),
      switchMap(query => {
        this.loading = true;
        this.errorMessage = '';
        return this.employeeService.searchEmployees({
          query,
          page: 0,
          size: 20,
          sortBy: 'name',
          sortDirection: 'ASC'
        }).pipe(
          timeout({ first: 15_000 }),
          catchError(() => {
            this.errorMessage = 'Unable to load employees. Check the backend connection and retry.';
            return of(null);
          }),
          finalize(() => this.loading = false)
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe(result => {
      if (!result) {
        this.employees = [];
        this.selectedEmployeeId = null;
        this.employeeSelected.emit(null);
        return;
      }

      if (!Array.isArray(result.content)) {
        this.employees = [];
        this.selectedEmployeeId = null;
        this.errorMessage = 'Employee search returned an invalid response. Retry the request.';
        this.employeeSelected.emit(null);
        return;
      }

      this.employees = result.content;
      this.selectedEmployeeId = null;
      this.employeeSelected.emit(null);
    });

    this.load();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.searchTerms.complete();
  }

  search(query: string): void {
    this.selectedEmployeeId = null;
    this.employeeSelected.emit(null);
    this.searchTerms.next(query.trim());
  }

  load(): void {
    this.searchTerms.next(this.query.trim());
  }

  select(id: number | null): void {
    this.selectedEmployeeId = id;
    this.employeeSelected.emit(this.employees.find(employee => employee.id === id) ?? null);
  }
}
