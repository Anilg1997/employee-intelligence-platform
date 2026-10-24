
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { mlApiUrl } from '../config/ml-api.config';
import { AttritionPredictionFeatures } from './prediction-features';

export type { AttritionPredictionFeatures as AttritionFeatures } from './prediction-features';

export interface AttritionPredictionResponse {
  attrition_prediction: string;
  attrition_probability: number;
}

export interface MlReadinessResponse {
  status: string;
  models: Record<string, string>;
}

@Injectable({
  providedIn: 'root'
})
export class AttritionMlService {
  private readonly http = inject(HttpClient);

  predict(
    features: AttritionPredictionFeatures
  ): Observable<AttritionPredictionResponse> {
    return this.http.post<AttritionPredictionResponse>(
      mlApiUrl('predict'),
      features
    );
  }

  checkReadiness(): Observable<MlReadinessResponse> {
    return this.http.get<MlReadinessResponse>(mlApiUrl('ready'));
  }
}
