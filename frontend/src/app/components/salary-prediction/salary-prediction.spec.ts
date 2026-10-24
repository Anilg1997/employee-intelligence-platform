import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Employee } from '../../models/employee';
import { SalaryPrediction } from './salary-prediction';

describe('SalaryPrediction', () => {
  let fixture: ComponentFixture<SalaryPrediction>;
  let component: SalaryPrediction;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SalaryPrediction],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    fixture = TestBed.createComponent(SalaryPrediction);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends validated source features and clears loading after a prediction', async () => {
    fixture.detectChanges();
    await new Promise(resolve => setTimeout(resolve, 275));
    http.expectOne(request =>
      request.url.endsWith('/api/employees/search') &&
      request.params.get('size') === '20'
    ).flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });

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
    component.onEmployeeSelected(employee);
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
      algorithm: 'random-forest-regressor'
    });

    expect(component.loading).toBe(false);
    expect(component.prediction?.estimated_monthly_income).toBe(5000);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('5000.00 dataset income units / month');
    expect(fixture.nativeElement.querySelector('button').textContent).toContain('Estimate salary');
  });
});
