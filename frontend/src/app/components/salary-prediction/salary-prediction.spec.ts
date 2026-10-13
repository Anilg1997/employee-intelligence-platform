import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { SalaryPrediction } from './salary-prediction';
import { EmployeeService } from '../../services/employee';
import { SalaryPredictionService } from '../../services/salary-prediction';
import { Employee } from '../../models/employee';

const employee: Employee = {
  id: 7, name: 'Taylor Reed', department: 'Engineering', jobRole: 'Developer', age: 34,
  education: 3, gender: 'Female', jobLevel: 2, maritalStatus: 'Single', overTime: 'No',
  percentSalaryHike: 15, performanceRating: 3, stockOptionLevel: 1,
  totalWorkingYears: 8, yearsAtCompany: 4
};

describe('SalaryPrediction', () => {
  let component: SalaryPrediction;
  let fixture: ComponentFixture<SalaryPrediction>;
  let employeeService: { getEmployees: ReturnType<typeof vi.fn> };
  let salaryService: { predict: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    employeeService = { getEmployees: vi.fn(() => of([employee])) };
    salaryService = { predict: vi.fn(() => of({
      estimated_monthly_income: 5000, model_version: 'salary-v2', algorithm: 'random-forest',
      limitations: ['Synthetic data'], metrics: { test: { mae: 840.65 } }
    })) };
    await TestBed.configureTestingModule({
      imports: [SalaryPrediction],
      providers: [
        { provide: EmployeeService, useValue: employeeService },
        { provide: SalaryPredictionService, useValue: salaryService }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(SalaryPrediction);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads employees and renders accessible controls and disclaimer', () => {
    expect(component.employees).toEqual([employee]);
    expect(fixture.nativeElement.querySelector('#salaryEmployee')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('button').getAttribute('aria-busy')).toBe('false');
    expect(fixture.nativeElement.textContent).toContain('Decision support only');
  });

  it('does not fabricate missing employee fields or call the API', () => {
    component.employees = [{ ...employee, overTime: undefined }];
    component.selectedEmployeeId = 7;
    component.predict();
    expect(salaryService.predict).not.toHaveBeenCalled();
    expect(component.missingFields).toContain('OverTime');
    expect(component.errorMessage).toContain('required by the salary model');
  });

  it('renders estimate, API metadata, metrics, limitations, and disclaimer', () => {
    component.predict();
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('5000.00');
    expect(text).toContain('salary-v2');
    expect(text).toContain('840.65');
    expect(text).toContain('Synthetic data');
    expect(text).toContain('not a fact about an employee');
  });

  it('shows a specific unavailable state for a 503 response', () => {
    salaryService.predict.mockReturnValue(throwError(() => ({ status: 503 })));
    component.predict();
    fixture.detectChanges();
    expect(component.prediction).toBeNull();
    expect(component.modelUnavailable).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('model is currently unavailable');
    expect(fixture.nativeElement.textContent).toContain('Try again');
  });

  it('shows employee load errors and empty state', () => {
    employeeService.getEmployees.mockReturnValue(throwError(() => new Error('offline')));
    component.loadEmployees();
    expect(component.employeeErrorMessage).toContain('Unable to load employees');
    employeeService.getEmployees.mockReturnValue(of([]));
    component.loadEmployees();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No employees are available');
  });

  it('rejects an invalid API estimate', () => {
    salaryService.predict.mockReturnValue(of({ estimated_monthly_income: NaN, model_version: 'v', algorithm: 'rf' }));
    component.predict();
    expect(component.prediction).toBeNull();
    expect(component.errorMessage).toContain('invalid estimate');
  });
});
