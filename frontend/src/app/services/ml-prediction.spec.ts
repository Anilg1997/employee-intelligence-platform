import { TestBed } from '@angular/core/testing';

import { MlPrediction } from './ml-prediction';

describe('MlPrediction', () => {
  let service: MlPrediction;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MlPrediction);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
