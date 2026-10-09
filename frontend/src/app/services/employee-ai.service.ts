import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api.config';

export interface EmployeeAiPrediction {
  attrition_prediction: string;
  attrition_probability: number;
}

export interface AttritionRiskResponse {
  employeeId: number;
  prediction: string;
  probability: number;
  riskLevel: string;
}

@Injectable({
  providedIn: 'root'
})
export class EmployeeAiService {

  private readonly employeeAiUrl = apiUrl('ai/employees');

  constructor(private http: HttpClient) {}

  predictEmployee(
    employeeId: number
  ): Observable<EmployeeAiPrediction> {

    return this.http.get<EmployeeAiPrediction>(
      `${this.employeeAiUrl}/${employeeId}/prediction`
    );
  }

  assessRisk(
    employeeId: number
  ): Observable<AttritionRiskResponse> {

    return this.http.get<AttritionRiskResponse>(
      `${this.employeeAiUrl}/${employeeId}/risk`
    );
  }
}
