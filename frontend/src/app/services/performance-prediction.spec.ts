import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { PerformancePredictionService } from './performance-prediction';
describe('PerformancePredictionService', () => {
  let service: PerformancePredictionService; let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [PerformancePredictionService, provideHttpClient(), provideHttpClientTesting()] }); service = TestBed.inject(PerformancePredictionService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('posts performance features without the target', () => {
    const request = { Age: 35, Department: 'Sales', JobRole: 'Manager', BusinessTravel: 'Travel_Rarely', Education: 3, OverTime: 'No' };
    service.predict(request).subscribe(result => expect(result.model_version).toBe('performance-classifier-v1'));
    const req = http.expectOne('http://localhost:8080/api/ml/predict/performance');
    expect(req.request.body.PerformanceRating).toBeUndefined();
    req.flush({ performance_prediction: 3, performance_probability: .8, class_probabilities: { '3': .8, '4': .2 }, model_version: 'performance-classifier-v1', algorithm: 'random-forest-classifier', limitations: ['synthetic'] });
  });
});
