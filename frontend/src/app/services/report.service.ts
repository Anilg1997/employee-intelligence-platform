import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { apiUrl } from '../config/api.config';
import { Report, ReportListResponse, ReportRequest } from '../models/report';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly reportsUrl = apiUrl('reports');

  list(): Observable<Report[]> {
    return this.http.get<Report[] | ReportListResponse>(this.reportsUrl).pipe(
      map(response => Array.isArray(response) ? response : response.reports ?? response.content ?? [])
    );
  }

  getById(id: number | string): Observable<Report> {
    return this.http.get<Report>(`${this.reportsUrl}/${encodeURIComponent(String(id))}`);
  }

  generate(request: ReportRequest): Observable<Report> {
    return this.http.post<Report>(this.reportsUrl, request);
  }
}
