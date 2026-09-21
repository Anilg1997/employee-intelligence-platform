import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HrAssistant } from './hr-assistant';

describe('HrAssistant', () => {
  let component: HrAssistant;
  let fixture: ComponentFixture<HrAssistant>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HrAssistant],
    }).compileComponents();

    fixture = TestBed.createComponent(HrAssistant);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
