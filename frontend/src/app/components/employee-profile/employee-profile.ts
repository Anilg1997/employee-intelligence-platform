
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subject, of } from 'rxjs';
import { catchError, finalize, switchMap, takeUntil } from 'rxjs/operators';
import { apiUrl } from '../../config/api.config';

interface Employee {
  id?: number;
  name?: string;
  age?: number;
  gender?: string;
  department?: string;
  jobRole?: string;
  jobLevel?: number;
  education?: number;
  educationField?: string;
  maritalStatus?: string;
  businessTravel?: string;
  monthlyIncome?: number;
  totalWorkingYears?: number;
  yearsAtCompany?: number;
  yearsInCurrentRole?: number;
  yearsWithCurrManager?: number;
  attrition?: string | null;
  overtime?: string;
  [key: string]: unknown;
}

@Component({
  selector: 'app-employee-profile',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './employee-profile.html',
  styleUrl: './employee-profile.scss'
})
export class EmployeeProfile implements OnInit, OnDestroy {
  employee: Employee | null = null;
  loading = true;
  errorMessage = '';

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly http: HttpClient,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        switchMap(params => {
          const id = Number(params.get('id'));

          this.employee = null;
          this.errorMessage = '';
          this.loading = true;

          if (!Number.isInteger(id) || id <= 0) {
            this.errorMessage = 'Invalid employee ID.';
            this.loading = false;
            return of(null);
          }

          return this.http
            .get<Employee>(apiUrl(`employees/${id}`))
            .pipe(
              catchError(error => {
                this.errorMessage =
                  error.status === 404
                    ? 'Employee not found.'
                    : 'Unable to load employee details. Check the backend and API URL.';

                return of(null);
              }),
              finalize(() => {
                this.loading = false;
                this.cdr.detectChanges();
              })
            );
        }),
        takeUntil(this.destroy$)
      )
      .subscribe(employee => {
        this.employee = employee;
        this.loading = false;
        this.cdr.detectChanges();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  display(value: unknown): string {
    if (value === null || value === undefined || value === '') {
      return 'Not available';
    }

    return String(value);
  }

  formatCurrency(value: unknown): string {
    if (
      value === null ||
      value === undefined ||
      value === '' ||
      !Number.isFinite(Number(value))
    ) {
      return 'Not available';
    }

    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(Number(value));
  }
}
