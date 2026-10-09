import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Employee } from '../models/employee';
import { EmployeePageResponse } from '../models/employee-page-response';
import { apiUrl } from '../config/api.config';
import { HttpParams } from '@angular/common/http';

export interface EmployeeSearchOptions {
  query?: string;
  department?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

  private readonly employeesUrl = apiUrl('employees');

  constructor(private http: HttpClient) {}

  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.employeesUrl);
  }

  searchEmployees(options: EmployeeSearchOptions = {}): Observable<EmployeePageResponse> {
    let params = new HttpParams();
    const values: Record<string, string | number | undefined> = {
      query: options.query?.trim() || undefined,
      department: options.department || undefined,
      page: options.page,
      size: options.size,
      sortBy: options.sortBy,
      sortDirection: options.sortDirection
    };

    Object.entries(values).forEach(([key, value]) => {
      if (value !== undefined) {
        params = params.set(key, String(value));
      }
    });

    return this.http.get<EmployeePageResponse>(`${this.employeesUrl}/search`, { params });
  }

  getEmployeeById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.employeesUrl}/${id}`);
  }

  createEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.employeesUrl, employee);
  }

  updateEmployee(id: number, employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(
      `${this.employeesUrl}/${id}`,
      employee
    );
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.employeesUrl}/${id}`);
  }
}
