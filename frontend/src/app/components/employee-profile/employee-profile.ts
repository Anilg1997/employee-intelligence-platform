import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { of, Subject } from 'rxjs';
import { catchError, finalize, switchMap, takeUntil } from 'rxjs/operators';

import { AuditEvent } from '../../models/audit-event';
import { Employee } from '../../models/employee';
import { RiskSummary } from '../../models/risk-summary';
import {
  AttritionRiskResponse,
  EmployeeAiService
} from '../../services/employee-ai.service';
import { AuditService } from '../../services/audit.service';
import { EmployeeService } from '../../services/employee';
import { RiskIntelligenceService } from '../../services/risk-intelligence';

@Component({
  selector: 'app-employee-profile',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './employee-profile.html',
  styleUrl: './employee-profile.scss'
})
export class EmployeeProfile implements OnInit, OnDestroy {
  employee: Employee | null = null;
  riskAssessment: AttritionRiskResponse | null = null;
  riskSummary: RiskSummary | null = null;
  loading = true;
  errorMessage = '';
  notFound = false;
  riskLoading = false;
  riskErrorMessage = '';
  summaryLoading = false;
  summaryErrorMessage = '';
  timelineEvents: AuditEvent[] = [];
  timelineLoading = false;
  timelineErrorMessage = '';

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly employeeService: EmployeeService,
    private readonly employeeAiService: EmployeeAiService,
    private readonly riskIntelligenceService: RiskIntelligenceService,
    private readonly auditService: AuditService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.paramMap.pipe(
      switchMap(params => {
        const id = Number(params.get('id'));
        this.employee = null;
        this.riskAssessment = null;
        this.riskSummary = null;
        this.timelineEvents = [];
        this.errorMessage = '';
        this.notFound = false;
        this.loading = true;

        if (!Number.isInteger(id) || id <= 0) {
          this.errorMessage = 'Invalid employee ID.';
          this.loading = false;
          return of({ id, employee: null });
        }

        return this.employeeService.getEmployeeById(id).pipe(
          catchError((error: { status?: number }) => {
            this.notFound = error?.status === 404;
            this.errorMessage = this.notFound
              ? 'Employee not found.'
              : 'Unable to load employee details. Check the backend and API URL.';
            return of(null);
          }),
          finalize(() => {
            this.loading = false;
            this.cdr.markForCheck();
          }),
          switchMap(employee => of({ id, employee }))
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe(({ id, employee }) => {
      this.employee = employee;
      this.loading = false;
      if (employee && Number.isInteger(id) && id > 0) {
        this.loadRisk(id);
        this.loadRiskSummary(id);
        this.loadTimeline(id);
      }
      this.cdr.markForCheck();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadTimeline(employeeId: number): void {
    this.timelineLoading = true;
    this.timelineErrorMessage = '';
    this.auditService.getEvents('', '', 0, 100).pipe(
      takeUntil(this.destroy$),
      finalize(() => this.cdr.markForCheck())
    ).subscribe({
      next: page => {
        this.timelineEvents = page.content.filter(event => event.resourceId === String(employeeId));
        this.timelineLoading = false;
      },
      error: () => {
        this.timelineLoading = false;
        this.timelineErrorMessage = 'Timeline and audit history are currently unavailable.';
      }
    });
  }

  private loadRiskSummary(id: number): void {
    this.summaryLoading = true;
    this.summaryErrorMessage = '';
    this.riskIntelligenceService.getSummary(id).pipe(
      takeUntil(this.destroy$),
      finalize(() => this.cdr.markForCheck())
    ).subscribe({
      next: summary => {
        this.riskSummary = summary;
        this.summaryLoading = false;
      },
      error: () => {
        this.summaryLoading = false;
        this.summaryErrorMessage = 'Employee 360 risk summary is currently unavailable.';
      }
    });
  }

  private loadRisk(id: number): void {
    this.riskLoading = true;
    this.riskErrorMessage = '';
    this.employeeAiService.assessRisk(id).pipe(
      takeUntil(this.destroy$),
      finalize(() => this.cdr.markForCheck())
    ).subscribe({
      next: assessment => {
        this.riskAssessment = assessment;
        this.riskLoading = false;
      },
      error: () => {
        this.riskLoading = false;
        this.riskErrorMessage = 'Risk assessment is currently unavailable.';
      }
    });
  }

  display(value: unknown): string {
    if (value === null || value === undefined || value === '') return 'Not available';
    return String(value);
  }

  formatValue(value: unknown): string {
    return this.display(value);
  }

  formatCurrency(value: unknown): string {
    if (value === null || value === undefined || value === '' || !Number.isFinite(Number(value))) {
      return 'Not available';
    }
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(Number(value));
  }

  formatTimestamp(timestamp: string): string {
    const date = new Date(timestamp);
    return Number.isNaN(date.getTime()) ? timestamp : date.toLocaleString();
  }
}
