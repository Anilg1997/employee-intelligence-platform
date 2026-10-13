import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import { SalaryPredictionService } from '../../services/salary-prediction';
import { SalaryPredictionRequest, SalaryPredictionResponse } from '../../models/salary-prediction';
@Component({ selector: 'app-salary-prediction', standalone: true, imports: [FormsModule], templateUrl: './salary-prediction.html', styleUrl: '../attrition-prediction/attrition-prediction.scss' })
export class SalaryPrediction implements OnInit {
  employees: Employee[] = [];
  selectedEmployeeId: number | null = null;
  prediction: SalaryPredictionResponse | null = null;
  employeesLoading = true;
  predictionLoading = false;
  employeeErrorMessage = '';
  errorMessage = '';
  modelUnavailable = false;
  missingFields: string[] = [];

  constructor(private readonly employeeService: EmployeeService, private readonly salaryService: SalaryPredictionService) {}

  ngOnInit(): void { this.loadEmployees(); }

  loadEmployees(): void {
    this.employeesLoading = true;
    this.employeeErrorMessage = '';
    this.employeeService.getEmployees().subscribe({
      next: employees => {
        this.employees = employees.filter(employee => employee.id !== undefined);
        this.selectedEmployeeId = this.employees[0]?.id ?? null;
        this.employeesLoading = false;
      },
      error: () => {
        this.employeesLoading = false;
        this.employeeErrorMessage = 'Unable to load employees for salary prediction.';
      }
    });
  }

  predict(): void {
    if (this.predictionLoading || this.employeesLoading) return;
    this.clearPrediction();
    const employee = this.employees.find(item => item.id === this.selectedEmployeeId);
    if (!employee) { this.errorMessage = 'Select an employee first.'; return; }
    const request = this.toRequest(employee);
    if (!request) {
      this.errorMessage = 'This employee does not have all fields required by the salary model.';
      return;
    }
    this.predictionLoading = true;
    this.salaryService.predict(request).subscribe({
      next: result => {
        if (!result || !Number.isFinite(result.estimated_monthly_income)) {
          this.errorMessage = 'The model returned an invalid estimate. No salary estimate is available.';
        } else {
          this.prediction = result;
        }
        this.predictionLoading = false;
      },
      error: error => {
        this.modelUnavailable = error?.status === 503;
        this.errorMessage = this.modelUnavailable
          ? 'The salary model is currently unavailable. No estimate was returned.'
          : 'Salary prediction failed. Check that the backend services are running and the employee data is valid.';
        this.predictionLoading = false;
      }
    });
  }

  clearPrediction(): void {
    this.prediction = null;
    this.errorMessage = '';
    this.modelUnavailable = false;
    this.missingFields = [];
  }

  metricEntries(metrics: SalaryPredictionResponse['metrics']): Array<{ name: string; value: string }> {
    return Object.entries(metrics ?? {}).map(([name, value]) => ({ name, value: typeof value === 'object' ? JSON.stringify(value) : String(value) }));
  }

  private toRequest(employee: Employee): SalaryPredictionRequest | null {
    const values = {
      Age: employee.age, Department: employee.department, JobLevel: employee.jobLevel,
      JobRole: employee.jobRole, TotalWorkingYears: employee.totalWorkingYears,
      YearsAtCompany: employee.yearsAtCompany, Education: employee.education,
      PercentSalaryHike: employee.percentSalaryHike, PerformanceRating: employee.performanceRating,
      Gender: employee.gender, MaritalStatus: employee.maritalStatus, OverTime: employee.overTime,
      StockOptionLevel: employee.stockOptionLevel
    };
    this.missingFields = Object.entries(values)
      .filter(([, value]) => value === undefined || value === null || value === '' || (typeof value === 'number' && !Number.isFinite(value)))
      .map(([field]) => field);
    return this.missingFields.length ? null : values as SalaryPredictionRequest;
  }
}
