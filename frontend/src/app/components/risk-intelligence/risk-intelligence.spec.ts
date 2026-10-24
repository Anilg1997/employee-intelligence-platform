import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import { RiskIntelligenceService } from '../../services/risk-intelligence';
import { RiskIntelligence } from './risk-intelligence';

const employee: Employee = {
  id: 7,
  name: 'Taylor Reed',
  department: 'Engineering',
  jobRole: 'Developer',
  age: 34
};

const summary = {
  employeeId: 7,
  attritionRisk: 0.72,
  performanceRisk: 0.4,
  promotionReadiness: 0.25,
  overallRisk: 0.62,
  promotionTargetType: 'portfolio_demo_rule_target',
  modelVersions: {
    attrition: 'attrition-v2',
    performance: 'performance-v1',
    promotionReadiness: 'rules-v1'
  },
  limitations: ['Not a calibrated probability', 'Synthetic training data']
};

describe('RiskIntelligence', () => {
  let component: RiskIntelligence;
  let fixture: ComponentFixture<RiskIntelligence>;
  let employeeService: { searchEmployees: ReturnType<typeof vi.fn> };
  let riskService: { getSummary: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    employeeService = {
      searchEmployees: vi.fn(() => of({
        content: [employee],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1
      }))
    };
    riskService = { getSummary: vi.fn(() => of(summary)) };
    await TestBed.configureTestingModule({
      imports: [RiskIntelligence],
      providers: [
        { provide: EmployeeService, useValue: employeeService },
        { provide: RiskIntelligenceService, useValue: riskService }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(RiskIntelligence);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.onEmployeeSelected(employee);
  });

  it('renders accessible employee selection and the human-review disclaimer', () => {
    expect(fixture.nativeElement.querySelector('#employeeSearch')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#predictionEmployee')).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Human review required');
  });

  it('loads a valid summary with risk bands, provenance, and limitations', () => {
    component.load();
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(riskService.getSummary).toHaveBeenCalledWith(7);
    expect(text).toContain('Moderate risk');
    expect(text).toContain('1 − class 4 performance score');
    expect(text).toContain('attrition-v2');
    expect(text).toContain('Synthetic training data');
    expect(text).toContain('Before deciding');
    expect(component.loading).toBe(false);
  });

  it('does not request a summary until an employee is selected', () => {
    component.onEmployeeSelected(null);
    component.load();
    expect(component.errorMessage).toContain('Select an employee');
    expect(riskService.getSummary).not.toHaveBeenCalled();
  });

  it('rejects an invalid summary response', () => {
    riskService.getSummary.mockReturnValue(of({ ...summary, overallRisk: 1.4 }));
    component.load();
    fixture.detectChanges();
    expect(component.summary).toBeNull();
    expect(component.invalidResponse).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('invalid summary');
  });

  it('shows service errors with a retry action', () => {
    riskService.getSummary.mockReturnValue(throwError(() => new Error('offline')));
    component.load();
    fixture.detectChanges();
    expect(component.summaryLoading).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Risk summary is unavailable');
    expect(fixture.nativeElement.textContent).toContain('Try again');
  });
});
