import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

  private readonly apiUrl =
    'http://localhost:8080/api/ai/employees';

  constructor(private http: HttpClient) {}

  predictEmployee(
    employeeId: number
  ): Observable<EmployeeAiPrediction> {

    return this.http.get<EmployeeAiPrediction>(
      `${this.apiUrl}/${employeeId}/prediction`
    );
  }

  assessRisk(
    employeeId: number
  ): Observable<AttritionRiskResponse> {

    return this.http.get<AttritionRiskResponse>(
      `${this.apiUrl}/${employeeId}/risk`
    );
  }
}