import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttritionPrediction } from './attrition-prediction';

describe('AttritionPrediction', () => {
  let component: AttritionPrediction;
  let fixture: ComponentFixture<AttritionPrediction>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AttritionPrediction],
    }).compileComponents();

    fixture = TestBed.createComponent(AttritionPrediction);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
