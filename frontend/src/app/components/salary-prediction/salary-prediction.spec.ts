import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Employee } from '../../models/employee';
import { SalaryPrediction } from './salary-prediction';

describe('SalaryPrediction', () => {
  let fixture: ComponentFixture<SalaryPrediction>;
  let component: SalaryPrediction;
  let http: HttpTestingController;

  const employee: Employee = {
    id: 13,
    name: 'Employee 13',
    department: 'Sales',
    jobRole: 'Sales Executive',
    age: 35,
    jobLevel: 2,
    totalWorkingYears: 10,
    yearsAtCompany: 5,
    education: 3,
    percentSalaryHike: 15,
    performanceRating: 3,
    gender: 'Female',
    maritalStatus: 'Single',
    overTime: 'No',
    stockOptionLevel: 1
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SalaryPrediction],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    fixture = TestBed.createComponent(SalaryPrediction);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    await new Promise(resolve => setTimeout(resolve, 275));
    http.expectOne(request =>
      request.url.endsWith('/api/employees/search') &&
      request.params.get('size') === '20'
    ).flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
    component.onEmployeeSelected(employee);
  });

  afterEach(() => http.verify());

  it('sends validated source features, displays the estimate, and clears loading', () => {
    component.predict();
    const request = http.expectOne(req => req.url.endsWith('/api/ml/predict/salary'));
    expect(request.request.body).toEqual({
      Age: 35,
      Department: 'Sales',
      JobLevel: 2,
      JobRole: 'Sales Executive',
      TotalWorkingYears: 10,
      YearsAtCompany: 5,
      Education: 3,
      PercentSalaryHike: 15,
      PerformanceRating: 3,
      Gender: 'Female',
      MaritalStatus: 'Single',
      OverTime: 'No',
      StockOptionLevel: 1
    });
    request.flush({
      estimated_monthly_income: 5000,
      model_version: 'salary-regression-v2',
      algorithm: 'random-forest-regressor',
      limitations: ['Synthetic data'],
      metrics: { test: { mae: 840.65 } }
    });
    fixture.detectChanges();

    expect(component.loading).toBe(false);
    expect(component.prediction?.estimated_monthly_income).toBe(5000);
    expect(fixture.nativeElement.textContent).toContain('5000.00');
    expect(fixture.nativeElement.textContent).toContain('Decision support only');
    expect(fixture.nativeElement.textContent).toContain('840.65');
    expect(fixture.nativeElement.textContent).toContain('Synthetic data');
  });

  it('does not call the API when required source fields are missing', () => {
    component.onEmployeeSelected({ ...employee, overTime: undefined });
    component.predict();
    expect(component.errorMessage).toContain('OverTime is missing');
    http.expectNone(request => request.url.endsWith('/api/ml/predict/salary'));
  });

  it('shows a specific unavailable state after a 503 response', () => {
    component.predict();
    http.expectOne(request => request.url.endsWith('/api/ml/predict/salary')).flush(
      'Unavailable',
      { status: 503, statusText: 'Unavailable' }
    );
    fixture.detectChanges();
    expect(component.prediction).toBeNull();
    expect(component.modelUnavailable).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('model is currently unavailable');
    expect(fixture.nativeElement.textContent).toContain('Try again');
  });

  it('rejects an invalid estimate returned by the API', () => {
    component.predict();
    http.expectOne(request => request.url.endsWith('/api/ml/predict/salary')).flush({
      estimated_monthly_income: Number.NaN,
      model_version: 'v1',
      algorithm: 'random forest'
    });
    expect(component.prediction).toBeNull();
    expect(component.errorMessage).toContain('invalid estimate');
  });
});
