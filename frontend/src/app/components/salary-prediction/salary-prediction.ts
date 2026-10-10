import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import { SalaryPredictionService } from '../../services/salary-prediction';
import { SalaryPredictionResponse } from '../../models/salary-prediction';
@Component({ selector: 'app-salary-prediction', standalone: true, imports: [FormsModule], templateUrl: './salary-prediction.html', styleUrl: '../attrition-prediction/attrition-prediction.scss' })
export class SalaryPrediction implements OnInit {
  employees: Employee[] = []; selectedEmployeeId: number | null = null; prediction: SalaryPredictionResponse | null = null; loading = false; errorMessage = '';
  constructor(private employeeService: EmployeeService, private salaryService: SalaryPredictionService) {}
  ngOnInit(): void { this.employeeService.getEmployees().subscribe({ next: e => { this.employees = e.filter(x => x.id !== undefined); this.selectedEmployeeId = this.employees[0]?.id ?? null; }, error: () => this.errorMessage = 'Unable to load employees for prediction.' }); }
  predict(): void { const employee = this.employees.find(x => x.id === this.selectedEmployeeId); if (!employee) { this.errorMessage = 'Select an employee first.'; return; } this.loading = true; this.errorMessage = ''; this.prediction = null; this.salaryService.predict({ Age: employee.age, Department: employee.department, JobLevel: employee.jobLevel ?? 1, JobRole: employee.jobRole, TotalWorkingYears: employee.totalWorkingYears ?? 5, YearsAtCompany: employee.yearsAtCompany ?? 1, Education: employee.education ?? 3, PercentSalaryHike: employee.percentSalaryHike ?? 15, PerformanceRating: employee.performanceRating ?? 3, Gender: employee.gender ?? 'Female', MaritalStatus: employee.maritalStatus ?? 'Single', OverTime: employee.overTime ?? 'No', StockOptionLevel: employee.stockOptionLevel ?? 0 }).subscribe({ next: r => { this.prediction = r; this.loading = false; }, error: () => { this.errorMessage = 'Prediction failed. Check that both backend services are running.'; this.loading = false; } }); }
}
