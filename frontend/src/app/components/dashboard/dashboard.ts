import { Component, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { apiUrl } from '../../config/api.config';

export interface DashboardResponse {
  totalEmployees: number;
  departmentStatistics: {
    [department: string]: number;
  };
}

export interface DepartmentSummary {
  name: string;
  count: number;
  share: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {

  dashboard: DashboardResponse | null = null;
  departmentSummaries: DepartmentSummary[] = [];

  loading = true;
  errorMessage = '';

  private readonly dashboardUrl = apiUrl('dashboard/summary');

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    this.http.get<DashboardResponse>(this.dashboardUrl).subscribe({

      next: (response) => {
        Promise.resolve().then(() => {
          this.dashboard = response;
          this.departmentSummaries = this.buildDepartmentSummaries(response);
          this.loading = false;
        });
      },

      error: () => {
        Promise.resolve().then(() => {
          this.errorMessage = 'Failed to load dashboard.';
          this.loading = false;
        });
      }

    });
  }

  private buildDepartmentSummaries(response: DashboardResponse): DepartmentSummary[] {
    const total = response.totalEmployees;

    return Object.entries(response.departmentStatistics ?? {})
      .map(([name, count]) => ({
        name,
        count,
        share: total > 0 ? (count / total) * 100 : 0
      }))
      .sort((first, second) => second.count - first.count || first.name.localeCompare(second.name));
  }

  get departmentCount(): number {
    return this.departmentSummaries.length;
  }

  get largestDepartment(): DepartmentSummary | null {
    return this.departmentSummaries[0] ?? null;
  }

  get averageDepartmentSize(): number {
    return this.departmentCount > 0
      ? (this.dashboard?.totalEmployees ?? 0) / this.departmentCount
      : 0;
  }

  get hasDepartmentData(): boolean {
    return this.departmentSummaries.length > 0;
  }

  formatShare(share: number): string {
    return `${share.toFixed(1)}%`;
  }
}
