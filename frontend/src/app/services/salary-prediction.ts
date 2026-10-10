import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api.config';
import { SalaryPredictionRequest, SalaryPredictionResponse } from '../models/salary-prediction';
@Injectable({ providedIn: 'root' })
export class SalaryPredictionService {
  private readonly url = apiUrl('ml/predict/salary');
  constructor(private http: HttpClient) {}
  predict(request: SalaryPredictionRequest): Observable<SalaryPredictionResponse> { return this.http.post<SalaryPredictionResponse>(this.url, request); }
}
