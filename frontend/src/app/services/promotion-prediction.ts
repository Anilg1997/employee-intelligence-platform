import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api.config';
import { PromotionPredictionRequest, PromotionPredictionResponse } from '../models/promotion-prediction';

@Injectable({ providedIn: 'root' })
export class PromotionPredictionService {
  private readonly url = apiUrl('ml/predict/promotion');
  constructor(private http: HttpClient) {}
  predict(request: PromotionPredictionRequest): Observable<PromotionPredictionResponse> {
    return this.http.post<PromotionPredictionResponse>(this.url, request);
  }
}
