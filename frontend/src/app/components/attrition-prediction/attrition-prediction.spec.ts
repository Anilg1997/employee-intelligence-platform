import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AttritionPrediction } from './attrition-prediction';
import { EmployeeService } from '../../services/employee';
import { MlPredictionService } from '../../services/ml-prediction';
import { Employee } from '../../models/employee';

const employee: Employee = {
  id: 7, name: 'Taylor Reed', department: 'Engineering', jobRole: 'Developer', age: 34,
  businessTravel: 'Travel_Rarely', dailyRate: 800, distanceFromHome: 5, education: 3,
  educationField: 'Technical Degree', environmentSatisfaction: 3, gender: 'Female', hourlyRate: 70,
  jobInvolvement: 3, jobLevel: 2, jobSatisfaction: 4, maritalStatus: 'Single', monthlyIncome: 5000,
  monthlyRate: 15000, numCompaniesWorked: 2, overTime: 'No', percentSalaryHike: 15,
  performanceRating: 3, relationshipSatisfaction: 3, stockOptionLevel: 1, totalWorkingYears: 8,
  trainingTimesLastYear: 3, workLifeBalance: 3, yearsAtCompany: 4, yearsInCurrentRole: 2,
  yearsSinceLastPromotion: 1, yearsWithCurrManager: 2
};

describe('AttritionPrediction', () => {
  let component: AttritionPrediction;
  let fixture: ComponentFixture<AttritionPrediction>;
  let employeeService: { getEmployees: ReturnType<typeof vi.fn> };
  let mlService: { predict: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    employeeService = { getEmployees: vi.fn(() => of([employee])) };
    mlService = { predict: vi.fn(() => of({
      attrition_prediction: 'Yes', attrition_probability: 0.72, model_version: 'v1',
      decision_threshold: 0.55, dataset: 'synthetic', training_provenance: 'validated split',
      limitations: ['Not a causal estimate']
    })) };
    await TestBed.configureTestingModule({
      imports: [AttritionPrediction],
      providers: [
        { provide: EmployeeService, useValue: employeeService },
        { provide: MlPredictionService, useValue: mlService }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(AttritionPrediction);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads employees and renders the decision-support disclaimer and controls', () => {
    expect(component.employees).toEqual([employee]);
    expect(fixture.nativeElement.textContent).toContain('Decision support only');
    expect(fixture.nativeElement.querySelector('#predictionEmployee')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('button').textContent).toContain('Predict attrition');
  });

  it('does not send a prediction request when model fields are missing', () => {
    component.employees = [{ ...employee, monthlyIncome: undefined }];
    component.selectedEmployeeId = 7;
    component.predict();
    expect(mlService.predict).not.toHaveBeenCalled();
    expect(component.missingFields).toContain('MonthlyIncome');
    expect(component.errorMessage).toContain('does not have all fields');
  });

  it('renders probability, threshold, provenance, limitations, and disclaimer from the API', () => {
    component.predict();
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('72.0%');
    expect(text).toContain('55%');
    expect(text).toContain('validated split');
    expect(text).toContain('Not a causal estimate');
    expect(text).toContain('not a fact about an employee');
  });

  it('shows an explicit unavailable-model state without a prediction', () => {
    mlService.predict.mockReturnValue(throwError(() => ({ status: 503 })));
    component.predict();
    fixture.detectChanges();
    expect(component.prediction).toBeNull();
    expect(component.modelUnavailable).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('model is currently unavailable');
    expect(fixture.nativeElement.textContent).toContain('Try again');
  });

  it('shows an empty state when the employee API returns no employees', () => {
    employeeService.getEmployees.mockReturnValue(of([]));
    component.loadEmployees();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No employees are available');
  });

  it('clears a previous result when the employee changes', () => {
    component.predict();
    component.clearPrediction();
    expect(component.prediction).toBeNull();
  });

  it('rejects a malformed probability rather than displaying a fabricated estimate', () => {
    mlService.predict.mockReturnValue(of({ attrition_prediction: 'Yes', attrition_probability: null }));
    component.predict();
    expect(component.prediction).toBeNull();
    expect(component.errorMessage).toContain('invalid prediction');
  });
});
