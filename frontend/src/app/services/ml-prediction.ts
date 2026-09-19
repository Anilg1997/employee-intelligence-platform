import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { MlPredictionRequest } from '../models/ml-prediction-request';
import { MlPredictionResponse } from '../models/ml-prediction-response';

@Injectable({
  providedIn: 'root'
})
export class MlPredictionService {

  private readonly apiUrl = 'http://localhost:8080/api/ml/predict';

  constructor(private http: HttpClient) {}

  predict(
    request: MlPredictionRequest
  ): Observable<MlPredictionResponse> {

    return this.http.post<MlPredictionResponse>(
      this.apiUrl,
      request
    );
  }
}