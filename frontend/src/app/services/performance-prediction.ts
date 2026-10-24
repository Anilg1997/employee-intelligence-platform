import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { timeout } from 'rxjs/operators';
import { apiUrl } from '../config/api.config';
import { PerformancePredictionRequest, PerformancePredictionResponse } from '../models/performance-prediction';
@Injectable({ providedIn: 'root' })
export class PerformancePredictionService {
  private readonly url = apiUrl('ml/predict/performance');
  constructor(private http: HttpClient) {}
  predict(request: PerformancePredictionRequest): Observable<PerformancePredictionResponse> {
    return this.http.post<PerformancePredictionResponse>(this.url, request).pipe(timeout({ first: 30_000 }));
  }
}
