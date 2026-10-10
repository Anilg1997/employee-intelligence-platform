import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { SalaryPredictionService } from './salary-prediction';

describe('SalaryPredictionService', () => {
  let service: SalaryPredictionService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [SalaryPredictionService, provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(SalaryPredictionService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('posts the leakage-safe salary contract and returns model metadata', () => {
    const request = { Age: 35, Department: 'Sales', JobLevel: 2, JobRole: 'Manager', TotalWorkingYears: 10, YearsAtCompany: 5, Education: 3, PercentSalaryHike: 15, PerformanceRating: 3, Gender: 'Female', MaritalStatus: 'Single', OverTime: 'No', StockOptionLevel: 1 };
    service.predict(request).subscribe(result => expect(result.model_version).toBe('salary-regression-v1'));
    const testRequest = http.expectOne('http://localhost:8080/api/ml/predict/salary');
    expect(testRequest.request.body).toEqual(request);
    expect(testRequest.request.body.MonthlyIncome).toBeUndefined();
    testRequest.flush({ estimated_monthly_income: 5000, model_version: 'salary-regression-v1', algorithm: 'random-forest-regressor' });
  });
});
