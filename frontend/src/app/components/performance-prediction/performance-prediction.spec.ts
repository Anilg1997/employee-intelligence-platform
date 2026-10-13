import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PerformancePrediction } from './performance-prediction';
import { EmployeeService } from '../../services/employee';
import { PerformancePredictionService } from '../../services/performance-prediction';
import { Employee } from '../../models/employee';

const employee: Employee = {
  id: 7, name: 'Taylor Reed', department: 'Sales', jobRole: 'Manager', age: 34,
  businessTravel: 'Travel_Rarely', dailyRate: 500, distanceFromHome: 4, education: 3,
  educationField: 'Medical', environmentSatisfaction: 3, gender: 'Female', hourlyRate: 60,
  jobInvolvement: 3, jobLevel: 2, jobSatisfaction: 3, maritalStatus: 'Single', monthlyIncome: 5000,
  monthlyRate: 14000, numCompaniesWorked: 1, overTime: 'No', percentSalaryHike: 15,
  relationshipSatisfaction: 3, stockOptionLevel: 1, totalWorkingYears: 8, trainingTimesLastYear: 3,
  workLifeBalance: 3, yearsAtCompany: 4, yearsInCurrentRole: 2, yearsSinceLastPromotion: 1, yearsWithCurrManager: 2
};

describe('PerformancePrediction', () => {
  let component: PerformancePrediction;
  let fixture: ComponentFixture<PerformancePrediction>;
  let employeeService: { getEmployees: ReturnType<typeof vi.fn> };
  let performanceService: { predict: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    employeeService = { getEmployees: vi.fn(() => of([employee])) };
    performanceService = { predict: vi.fn(() => of({ performance_prediction: 3, performance_probability: .8, class_probabilities: { '3': .8, '4': .2 }, model_version: 'performance-classifier-v1', algorithm: 'random-forest-classifier', metrics: { test: { accuracy: .7 } }, limitations: ['Synthetic data'] })) };
    await TestBed.configureTestingModule({ imports: [PerformancePrediction], providers: [{ provide: EmployeeService, useValue: employeeService }, { provide: PerformancePredictionService, useValue: performanceService }] }).compileComponents();
    fixture = TestBed.createComponent(PerformancePrediction); component = fixture.componentInstance; fixture.detectChanges();
  });

  it('does not fabricate missing fields or call the model', () => {
    component.employees = [{ ...employee, hourlyRate: undefined }]; component.selectedEmployeeId = 7; component.predict();
    expect(performanceService.predict).not.toHaveBeenCalled(); expect(component.missingFields).toContain('HourlyRate');
  });

  it('renders class distribution, metadata, metrics, limitations and disclaimer', () => {
    component.predict(); fixture.detectChanges(); const text = fixture.nativeElement.textContent;
    expect(text).toContain('Predicted rating class: 3'); expect(text).toContain('Rating 4'); expect(text).toContain('0.7');
    expect(text).toContain('performance-classifier-v1'); expect(text).toContain('Synthetic data'); expect(text).toContain('not calibrated confidence');
  });

  it('handles model unavailable and invalid results', () => {
    performanceService.predict.mockReturnValue(throwError(() => ({ status: 503 }))); component.predict(); fixture.detectChanges();
    expect(component.modelUnavailable).toBe(true); expect(fixture.nativeElement.textContent).toContain('currently unavailable');
    performanceService.predict.mockReturnValue(of({ performance_prediction: 3 })); component.predict();
    expect(component.prediction).toBeNull(); expect(component.errorMessage).toContain('incomplete or invalid');
  });

  it('renders employee load error and empty state', () => {
    employeeService.getEmployees.mockReturnValue(throwError(() => new Error('offline'))); component.loadEmployees();
    expect(component.employeeErrorMessage).toContain('Unable to load employees'); employeeService.getEmployees.mockReturnValue(of([])); component.loadEmployees(); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No employees are available');
  });
});
