import { Component, OnInit } from '@angular/core';
import { KeyValuePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface DashboardResponse {
  totalEmployees: number;
  departmentStatistics: {
    [department: string]: number;
  };
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [KeyValuePipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {

  dashboard: DashboardResponse | null = null;

  loading = true;
  errorMessage = '';

  private readonly apiUrl =
    'http://localhost:8080/api/dashboard/summary';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {

    this.loading = true;
    this.errorMessage = '';

    this.http.get<DashboardResponse>(this.apiUrl).subscribe({

      next: (response) => {
        console.log('Dashboard response:', response);

        this.dashboard = response;
        this.loading = false;
      },

      error: (error) => {
        console.error('Dashboard error:', error);

        this.errorMessage = 'Failed to load dashboard.';
        this.loading = false;
      }

    });
  }
}