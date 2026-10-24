
import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';

import {
  Chart,
  ChartConfiguration,
  ChartData,
  registerables
} from 'chart.js';

import { apiUrl } from '../../config/api.config';

Chart.register(...registerables);

export interface DashboardResponse {
  totalEmployees: number;
  departmentStatistics: Record<string, number>;
}

export interface DepartmentSummary {
  name: string;
  count: number;
  share: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    DecimalPipe,
    BaseChartDirective,
    RouterLink
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  dashboard: DashboardResponse | null = null;

  departmentSummaries: DepartmentSummary[] = [];

  selectedDepartmentName = '';

  loading = true;

  errorMessage = '';

  private readonly dashboardUrl =
    apiUrl('dashboard/summary');

  readonly doughnutType = 'doughnut' as const;

  readonly barType = 'bar' as const;

  readonly doughnutOptions:
    ChartConfiguration<'doughnut'>['options'] = {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '68%',

      onClick: (_event, elements) => {
        const index = elements[0]?.index;

        if (index === undefined) {
          return;
        }

        const department =
          this.visibleDepartmentSummaries[index];

        if (department) {
          this.toggleDepartmentFilter(department.name);
        }
      },

      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            usePointStyle: true,
            pointStyle: 'circle',
            padding: 18,
            boxWidth: 9,
            boxHeight: 9,
            color: '#52627d',
            font: {
              size: 12,
              weight: 500
            }
          }
        },

        tooltip: {
          backgroundColor: '#17233c',
          titleColor: '#ffffff',
          bodyColor: '#e8eef9',
          padding: 12,
          cornerRadius: 8,

          callbacks: {
            label: (context) => {
              const value = Number(context.raw ?? 0);

              const total =
                this.dashboard?.totalEmployees ?? 0;

              const share =
                total > 0 ? (value / total) * 100 : 0;

              return ` ${context.label}: ${value.toLocaleString()} (${share.toFixed(1)}% of workforce)`;
            }
          }
        }
      }
    };

  doughnutData: ChartData<
    'doughnut',
    number[],
    string
  > = {
    labels: [],

    datasets: [{
      data: [],

      backgroundColor: [
        '#4568dc',
        '#16a085',
        '#f5a623',
        '#8b5cf6',
        '#e76f51',
        '#0ea5e9',
        '#64748b'
      ],

      borderColor: '#ffffff',
      borderWidth: 3,
      hoverOffset: 8
    }]
  };

  readonly barOptions:
    ChartConfiguration<'bar'>['options'] = {
      indexAxis: 'y',
      responsive: true,
      maintainAspectRatio: false,

      onClick: (_event, elements) => {
        const index = elements[0]?.index;

        if (index === undefined) {
          return;
        }

        const department =
          this.visibleDepartmentSummaries[index];

        if (department) {
          this.toggleDepartmentFilter(department.name);
        }
      },

      plugins: {
        legend: {
          display: false
        },

        tooltip: {
          backgroundColor: '#17233c',
          titleColor: '#ffffff',
          bodyColor: '#e8eef9',
          padding: 12,
          cornerRadius: 8,
          displayColors: false,

          callbacks: {
            label: (context) =>
              ` ${Number(context.raw ?? 0).toLocaleString()} employees`
          }
        }
      },

      scales: {
        x: {
          beginAtZero: true,

          border: {
            display: false
          },

          grid: {
            color: '#edf1f7'
          },

          ticks: {
            precision: 0,
            color: '#7a879b',

            callback: (value) =>
              Number(value).toLocaleString()
          },

          title: {
            display: true,
            text: 'Number of employees',
            color: '#64748b'
          }
        },

        y: {
          border: {
            display: false
          },

          grid: {
            display: false
          },

          ticks: {
            autoSkip: false,
            color: '#52627d',
            padding: 8
          }
        }
      }
    };

  barData: ChartData<
    'bar',
    number[],
    string
  > = {
    labels: [],

    datasets: [{
      label: 'Employees',
      data: [],
      backgroundColor: '#4568dc',
      hoverBackgroundColor: '#2948ad',
      borderRadius: 6,
      borderSkipped: false,
      barPercentage: 0.68,
      categoryPercentage: 0.78
    }]
  };

  constructor(
    private readonly http: HttpClient,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    this.http
      .get<DashboardResponse>(this.dashboardUrl)
      .subscribe({
        next: (response) => {
          try {
            this.dashboard = response;

            this.departmentSummaries =
              this.buildDepartmentSummaries(response);

            this.updateCharts();

            this.loading = false;
            this.cdr.detectChanges();

          } catch {
            this.errorMessage =
              'Failed to prepare dashboard data.';

            this.loading = false;
            this.cdr.detectChanges();
          }
        },

        error: () => {
          this.errorMessage =
            'Could not load workforce data. Verify that the backend is running on port 8080.';

          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  private buildDepartmentSummaries(
    response: DashboardResponse
  ): DepartmentSummary[] {
    const total = response.totalEmployees;

    return Object.entries(
      response.departmentStatistics ?? {}
    )
      .map(([rawName, count]) => ({
        name: rawName.trim() || 'Unspecified',
        count: Number(count) || 0
      }))
      .reduce((departments, current) => {
        const existing = departments.find(
          item =>
            item.name.toLowerCase() ===
            current.name.toLowerCase()
        );

        if (existing) {
          existing.count += current.count;
        } else {
          departments.push({ ...current });
        }

        return departments;
      }, [] as { name: string; count: number }[])
      .map(({ name, count }) => ({
        name,
        count,
        share: total > 0
          ? (count / total) * 100
          : 0
      }))
      .sort(
        (a, b) =>
          b.count - a.count ||
          a.name.localeCompare(b.name)
      );
  }

  get visibleDepartmentSummaries():
    DepartmentSummary[] {
    if (!this.selectedDepartmentName) {
      return this.departmentSummaries;
    }

    return this.departmentSummaries.filter(
      department =>
        department.name === this.selectedDepartmentName
    );
  }

  get selectedDepartment():
    DepartmentSummary | null {
    return this.departmentSummaries.find(
      department =>
        department.name === this.selectedDepartmentName
    ) ?? null;
  }

  get departmentCount(): number {
    return this.departmentSummaries.length;
  }

  get visibleDepartmentCount(): number {
    return this.visibleDepartmentSummaries.length;
  }

  get largestDepartment():
    DepartmentSummary | null {
    return this.visibleDepartmentSummaries[0] ?? null;
  }

  get averageDepartmentSize(): number {
    const departments =
      this.visibleDepartmentSummaries;

    return departments.length > 0
      ? departments.reduce(
          (sum, department) => sum + department.count,
          0
        ) / departments.length
      : 0;
  }

  get hasDepartmentData(): boolean {
    return this.departmentSummaries.length > 0;
  }

  get barChartHeight(): number {
    return Math.max(
      300,
      this.visibleDepartmentCount * 58
    );
  }

  onDepartmentFilterChange(event: Event): void {
    const select =
      event.target as HTMLSelectElement;

    this.selectedDepartmentName = select.value;
    this.updateCharts();
  }

  toggleDepartmentFilter(name: string): void {
    this.selectedDepartmentName =
      this.selectedDepartmentName === name
        ? ''
        : name;

    this.updateCharts();
  }

  clearDepartmentFilter(): void {
    this.selectedDepartmentName = '';
    this.updateCharts();
  }

  private updateCharts(): void {
    const departments =
      this.visibleDepartmentSummaries;

    const labels =
      departments.map(item => item.name);

    const values =
      departments.map(item => item.count);

    this.doughnutData = {
      ...this.doughnutData,
      labels,

      datasets: [{
        ...this.doughnutData.datasets[0],
        data: [...values]
      }]
    };

    this.barData = {
      ...this.barData,
      labels,

      datasets: [{
        ...this.barData.datasets[0],
        data: [...values]
      }]
    };
  }

  formatShare(share: number): string {
    return `${share.toFixed(1)}%`;
  }
}
