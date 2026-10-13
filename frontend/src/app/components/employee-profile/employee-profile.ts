import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import {
  AttritionRiskResponse,
  EmployeeAiService
} from '../../services/employee-ai.service';
import { RiskIntelligenceService } from '../../services/risk-intelligence';
import { RiskSummary } from '../../models/risk-summary';
import { AuditEvent } from '../../models/audit-event';
import { AuditService } from '../../services/audit.service';

@Component({
  selector: 'app-employee-profile',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './employee-profile.html',
  styleUrl: './employee-profile.scss'
})
export class EmployeeProfile implements OnInit {
  employee: Employee | null = null;
  riskAssessment: AttritionRiskResponse | null = null;
  riskSummary: RiskSummary | null = null;
  loading = true;
  riskLoading = false;
  errorMessage = '';
  notFound = false;
  riskErrorMessage = '';
  summaryLoading = false;
  summaryErrorMessage = '';
  timelineEvents: AuditEvent[] = [];
  timelineLoading = false;
  timelineErrorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly employeeService: EmployeeService,
    private readonly employeeAiService: EmployeeAiService,
    private readonly riskIntelligenceService: RiskIntelligenceService,
    private readonly auditService: AuditService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id < 1) {
      this.loading = false;
      this.notFound = true;
      return;
    }

    this.employeeService.getEmployeeById(id).subscribe({
      next: (employee) => {
        this.employee = employee;
        this.loading = false;
        this.loadRisk(id);
        this.loadRiskSummary(id);
        this.loadTimeline(id);
      },
      error: (error: { status?: number }) => {
        this.loading = false;
        if (error?.status === 404) {
          this.notFound = true;
        } else {
          this.errorMessage = 'Unable to load this employee profile.';
        }
      }
    });
  }

  private loadTimeline(employeeId: number): void {
    this.timelineLoading = true;
    this.timelineErrorMessage = '';
    this.auditService.getEvents('', '', 0, 100).subscribe({
      next: (page) => {
        // The existing audit contract is global and exposes resourceId, so only
        // display events explicitly associated with this employee.
        this.timelineEvents = page.content.filter((event) => event.resourceId === String(employeeId));
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
    this.riskIntelligenceService.getSummary(id).subscribe({
      next: (summary) => { this.riskSummary = summary; this.summaryLoading = false; },
      error: () => {
        this.summaryLoading = false;
        this.summaryErrorMessage = 'Employee 360 risk summary is currently unavailable.';
      }
    });
  }

  private loadRisk(id: number): void {
    this.riskLoading = true;
    this.riskErrorMessage = '';
    this.employeeAiService.assessRisk(id).subscribe({
      next: (assessment) => {
        this.riskAssessment = assessment;
        this.riskLoading = false;
      },
      error: () => {
        this.riskLoading = false;
        this.riskErrorMessage = 'Risk assessment is currently unavailable.';
      }
    });
  }

  formatValue(value: string | number | undefined): string {
    return value === undefined || value === null || value === '' ? 'Not available' : String(value);
  }

  formatTimestamp(timestamp: string): string {
    const date = new Date(timestamp);
    return Number.isNaN(date.getTime()) ? timestamp : date.toLocaleString();
  }
}
