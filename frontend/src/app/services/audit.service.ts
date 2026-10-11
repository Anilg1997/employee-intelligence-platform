import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api.config';
import { AuditPage } from '../models/audit-event';
@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  getEvents(action = '', result = '', page = 0, size = 25): Observable<AuditPage> {
    const params = new HttpParams({ fromObject: { action, result, page: String(page), size: String(size) } });
    return this.http.get<AuditPage>(apiUrl('audit/events'), { params });
  }
}
