import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api.config';
import { ActuatorHealth, ActuatorMetricNames, DocumentSearchResult, ModelMetadata } from '../models/operations';

@Injectable({ providedIn: 'root' })
export class OperationsService {
  private readonly http = inject(HttpClient);
  private actuatorUrl(path: string): string { const base = apiUrl('/'); return `${base.replace(/\/api\/?$/, '')}/${path.replace(/^\//, '')}`; }
  models(): Observable<ModelMetadata[]> { return this.http.get<ModelMetadata[]>(apiUrl('/ml/models')); }
  health(): Observable<ActuatorHealth> { return this.http.get<ActuatorHealth>(this.actuatorUrl('/actuator/health')); }
  metricNames(): Observable<ActuatorMetricNames> { return this.http.get<ActuatorMetricNames>(this.actuatorUrl('/actuator/metrics')); }
  metric(name: string): Observable<Record<string, unknown>> { return this.http.get<Record<string, unknown>>(this.actuatorUrl(`/actuator/metrics/${encodeURIComponent(name)}`)); }
  searchDocuments(query: string, limit = 20): Observable<DocumentSearchResult[]> {
    const params = new HttpParams().set('query', query).set('limit', limit);
    return this.http.get<DocumentSearchResult[]>(apiUrl('/rag/documents/search'), { params });
  }
  ingestDocument(filePath: string, source: string): Observable<{ message: string }> {
    const params = new HttpParams().set('filePath', filePath).set('source', source);
    return this.http.post<{ message: string }>(apiUrl('/rag/ingestion'), null, { params });
  }
}
