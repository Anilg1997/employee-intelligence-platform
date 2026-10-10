import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { PromotionPredictionService } from './promotion-prediction';

describe('PromotionPredictionService', () => {
  let service: PromotionPredictionService;
  let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [PromotionPredictionService, provideHttpClient(), provideHttpClientTesting()] }); service = TestBed.inject(PromotionPredictionService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('posts to the promotion proxy endpoint', () => {
    service.predict({ YearsAtCompany: 4 }).subscribe(result => expect(result.promotion_prediction).toBe('Ready'));
    const request = http.expectOne('http://localhost:8080/api/ml/predict/promotion');
    expect(request.request.method).toBe('POST'); request.flush({ promotion_prediction: 'Ready' });
  });
});
