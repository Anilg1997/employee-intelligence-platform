import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { timeout } from 'rxjs/operators';
import { apiUrl } from '../config/api.config';
import { RiskSummary } from '../models/risk-summary';

@Injectable({ providedIn: 'root' })
export class RiskIntelligenceService {
  constructor(private readonly http: HttpClient) {}

  getSummary(id: number): Observable<RiskSummary> {
    return this.http.get<RiskSummary>(apiUrl(`ai/employees/${id}/risk-summary`))
      .pipe(timeout({ first: 60_000 }));
  }
}
