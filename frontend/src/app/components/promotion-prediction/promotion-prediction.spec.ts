import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import { PromotionPredictionService } from '../../services/promotion-prediction';
import { PromotionPrediction } from './promotion-prediction';

const employee: Employee = {
  id: 7, name: 'Taylor Reed', department: 'Sales', jobRole: 'Manager', age: 34,
  businessTravel: 'Travel_Rarely', dailyRate: 500, distanceFromHome: 4, education: 3,
  educationField: 'Medical', environmentSatisfaction: 3, gender: 'Female', hourlyRate: 60,
  jobInvolvement: 3, jobLevel: 2, jobSatisfaction: 3, maritalStatus: 'Single', monthlyIncome: 5000,
  monthlyRate: 14000, numCompaniesWorked: 1, overTime: 'No', percentSalaryHike: 15,
  performanceRating: 3, relationshipSatisfaction: 3, stockOptionLevel: 1, totalWorkingYears: 8,
  trainingTimesLastYear: 3, workLifeBalance: 3, yearsAtCompany: 4, yearsInCurrentRole: 2,
  yearsSinceLastPromotion: 1, yearsWithCurrManager: 2
};

const result = {
  promotion_prediction: 'Ready', promotion_probability: .75, rule_score: .75, decision_threshold: .75,
  rule_checks: { tenure: true, recency: false, performance: true, level: true },
  model_version: 'promotion-rule-v1', algorithm: 'deterministic-policy', target_type: 'portfolio_demo_rule_target',
  limitations: ['Synthetic data', 'Requires human review']
};

describe('PromotionPrediction', () => {
  let component: PromotionPrediction;
  let fixture: ComponentFixture<PromotionPrediction>;
  let employeeService: { getEmployees: ReturnType<typeof vi.fn> };
  let promotionService: { predict: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    employeeService = { getEmployees: vi.fn(() => of([employee])) };
    promotionService = { predict: vi.fn(() => of(result)) };
    await TestBed.configureTestingModule({ imports: [PromotionPrediction], providers: [
      { provide: EmployeeService, useValue: employeeService }, { provide: PromotionPredictionService, useValue: promotionService }
    ] }).compileComponents();
    fixture = TestBed.createComponent(PromotionPrediction); component = fixture.componentInstance; fixture.detectChanges();
  });

  it('does not fabricate missing fields or call the model', () => {
    component.employees = [{ ...employee, performanceRating: undefined }]; component.selectedEmployeeId = 7; component.predict();
    expect(promotionService.predict).not.toHaveBeenCalled(); expect(component.missingFields).toContain('PerformanceRating');
  });

  it('renders score, threshold, checks, metadata, limitations and disclaimer', () => {
    component.predict(); fixture.detectChanges(); const text = fixture.nativeElement.textContent;
    expect(text).toContain('Ready'); expect(text).toContain('75.0%'); expect(text).toContain('Decision threshold');
    expect(text).toContain('Met'); expect(text).toContain('promotion-rule-v1'); expect(text).toContain('Synthetic data');
    expect(text).toContain('non-calibrated');
  });

  it('handles unavailable and invalid model responses', () => {
    promotionService.predict.mockReturnValue(throwError(() => ({ status: 503 }))); component.predict(); fixture.detectChanges();
    expect(component.modelUnavailable).toBe(true); expect(fixture.nativeElement.textContent).toContain('currently unavailable');
    component.clearPrediction(); fixture.detectChanges(false);
    promotionService.predict.mockReturnValue(of({ promotion_prediction: 'Ready' })); component.predict(); fixture.detectChanges(false);
    expect(component.prediction).toBeNull(); expect(component.errorMessage).toContain('incomplete or invalid');
  });

  it('renders employee loading failure and empty states', () => {
    employeeService.getEmployees.mockReturnValue(throwError(() => new Error('offline'))); component.loadEmployees(); fixture.detectChanges();
    expect(component.employeeErrorMessage).toContain('Unable to load employees');
    employeeService.getEmployees.mockReturnValue(of([]));
    const emptyFixture = TestBed.createComponent(PromotionPrediction); emptyFixture.detectChanges();
    expect(emptyFixture.nativeElement.textContent).toContain('No employees are available');
  });
});
