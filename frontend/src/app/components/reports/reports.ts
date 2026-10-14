import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Report, ReportRequest, ReportType } from '../../models/report';
import { ReportService } from '../../services/report.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './reports.html',
  styleUrl: './reports.scss'
})
export class Reports implements OnInit {
  private readonly service = inject(ReportService);
  readonly reportTypes: { value: ReportType; label: string }[] = [
    { value: 'WORKFORCE', label: 'Workforce summary' },
    { value: 'EMPLOYEE', label: 'Employee profile' },
    { value: 'DEPARTMENT', label: 'Department analysis' }
  ];
  reports: Report[] = [];
  selectedReport: Report | null = null;
  reportType: ReportType = 'WORKFORCE';
  employeeId = '';
  department = '';
  loading = true;
  generating = false;
  errorMessage = '';
  generateError = '';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.service.list().subscribe({
      next: reports => { this.reports = reports; this.loading = false; },
      error: () => { this.errorMessage = 'Reports are unavailable. Check the backend connection and try again.'; this.loading = false; }
    });
  }

  generate(): void {
    this.generating = true;
    this.generateError = '';
    const request: ReportRequest = { reportType: this.reportType };
    const employeeId = Number.parseInt(this.employeeId.trim(), 10);
    if (Number.isInteger(employeeId) && employeeId > 0) request.employeeId = employeeId;
    if (this.department.trim()) request.department = this.department.trim();
    this.service.generate(request).subscribe({
      next: report => { this.reports = [report, ...this.reports.filter(item => item.id !== report.id)]; this.selectedReport = report; this.generating = false; },
      error: () => { this.generateError = 'The report could not be generated. Please try again.'; this.generating = false; }
    });
  }

  selectReport(report: Report): void {
    this.selectedReport = report;
    this.service.getById(report.id).subscribe({ next: detail => this.selectedReport = detail });
  }

  statusLabel(status: string): string { return status.replaceAll('_', ' ').toLowerCase().replace(/^./, value => value.toUpperCase()); }
  typeLabel(type: string): string { return type.replaceAll('_', ' ').toLowerCase().replace(/^./, value => value.toUpperCase()); }
}
