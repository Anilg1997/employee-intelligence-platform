
import { CommonModule, DecimalPipe } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  Component,
  OnDestroy,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, of } from 'rxjs';

import {
  catchError,
  debounceTime,
  switchMap,
  takeUntil
} from 'rxjs/operators';

import { apiUrl } from '../../config/api.config';

interface Employee {
  id: number;
  name: string;
  department?: string | null;
  jobRole?: string | null;
  age?: number | null;
  attrition?: string | null;

  gender?: string | null;
  maritalStatus?: string | null;
  education?: number | null;
  educationField?: string | null;
  jobLevel?: number | null;
  monthlyIncome?: number | null;
  yearsAtCompany?: number | null;
  yearsInCurrentRole?: number | null;
  yearsWithCurrManager?: number | null;
  totalWorkingYears?: number | null;
  businessTravel?: string | null;
  overtime?: string | null;

  [key: string]: unknown;
}

interface EmployeePageResponse {
  content: Employee[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

interface DashboardSummaryResponse {
  departmentStatistics: Record<string, number>;
}

@Component({
  selector: 'app-employee-directory',
  standalone: true,
  imports: [
    CommonModule,
    DecimalPipe,
    FormsModule,
    RouterLink
  ],
  templateUrl: './employee-directory.html',
  styleUrl: './employee-directory.scss'
})
export class EmployeeDirectory implements OnInit, OnDestroy {
  private readonly searchUrl = apiUrl('employees/search');
  private readonly summaryUrl = apiUrl('dashboard/summary');

  private readonly searchChanges = new Subject<void>();
  private readonly destroy$ = new Subject<void>();

  employees: Employee[] = [];
  departments: string[] = [];

  searchText = '';
  selectedDepartment = '';

  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;

  sortBy = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  loading = false;
  errorMessage = '';
  selectedEmployee: Employee | null = null;

  readonly pageSizes = [10, 20, 50];

  constructor(
    private readonly http: HttpClient,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.searchChanges
  .pipe(
    debounceTime(300),
    switchMap(() => {
          this.loading = true;
          this.errorMessage = '';
          this.cdr.detectChanges();

          return this.http
            .get<EmployeePageResponse>(
              this.searchUrl,
              { params: this.buildParams() }
            )
            .pipe(
              catchError(() => {
                this.errorMessage =
                  'Unable to load employees. Check that the backend is running and try again.';

                return of(null);
              })
            );
        }),
        takeUntil(this.destroy$)
      )
      .subscribe((response) => {
        if (response) {
          this.employees = response.content ?? [];
          this.page = response.page ?? 0;
          this.size = response.size ?? this.size;
          this.totalElements = response.totalElements ?? 0;
          this.totalPages = response.totalPages ?? 0;
        } else {
          this.employees = [];
          this.totalElements = 0;
          this.totalPages = 0;
        }

        this.loading = false;
        this.cdr.detectChanges();
      });

    this.loadDepartments();
    this.loadEmployees();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.searchChanges.complete();
  }

  private buildParams(): HttpParams {
    let params = new HttpParams()
      .set('page', String(this.page))
      .set('size', String(this.size))
      .set('sortBy', this.sortBy)
      .set('sortDirection', this.sortDirection);

    const query = this.searchText.trim();

    if (query) {
      params = params.set('query', query);
    }

    if (this.selectedDepartment) {
      params = params.set(
        'department',
        this.selectedDepartment
      );
    }

    return params;
  }

  loadEmployees(): void {
    this.searchChanges.next();
  }

  private loadDepartments(): void {
    this.http
      .get<DashboardSummaryResponse>(this.summaryUrl)
      .pipe(
        catchError(() => {
          return of(null);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe((summary) => {
        const statistics =
          summary?.departmentStatistics ?? {};

        this.departments = [
          ...new Set(
            Object.keys(statistics)
              .map(name => name.trim())
              .filter(Boolean)
          )
        ].sort((a, b) => a.localeCompare(b));

        this.cdr.detectChanges();
      });
  }

  onSearchChange(): void {
    this.page = 0;
    this.loadEmployees();
  }

  onDepartmentChange(): void {
    this.page = 0;
    this.loadEmployees();
  }

  onPageSizeChange(): void {
    this.page = 0;
    this.loadEmployees();
  }

  goToPage(nextPage: number): void {
    if (
      nextPage < 0 ||
      nextPage >= this.totalPages ||
      nextPage === this.page
    ) {
      return;
    }

    this.page = nextPage;
    this.loadEmployees();
  }

  get firstVisibleRecord(): number {
    return this.totalElements === 0
      ? 0
      : this.page * this.size + 1;
  }

  get lastVisibleRecord(): number {
    return Math.min(
      (this.page + 1) * this.size,
      this.totalElements
    );
  }

  get pageNumbers(): number[] {
    const start = Math.max(0, this.page - 2);
    const end = Math.min(
      this.totalPages,
      start + 5
    );

    const adjustedStart = Math.max(0, end - 5);

    return Array.from(
      { length: end - adjustedStart },
      (_, index) => adjustedStart + index
    );
  }

  sortByColumn(column: string): void {
    if (this.sortBy === column) {
      this.sortDirection =
        this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDirection = 'asc';
    }

    this.page = 0;
    this.loadEmployees();
  }

  sortIndicator(column: string): string {
    if (this.sortBy !== column) {
      return '↕';
    }

    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  openEmployeeDetails(employee: Employee): void {
    this.selectedEmployee = employee;
  }

  closeEmployeeDetails(): void {
    this.selectedEmployee = null;
  }

  clearFilters(): void {
    this.searchText = '';
    this.selectedDepartment = '';
    this.page = 0;
    this.loadEmployees();
  }

  displayValue(value: unknown): string {
    if (value === null || value === undefined || value === '') {
      return 'Not available';
    }

    return String(value);
  }

  trackEmployee(
    _index: number,
    employee: Employee
  ): number {
    return employee.id;
  }
}
