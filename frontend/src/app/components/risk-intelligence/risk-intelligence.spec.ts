import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RiskIntelligence } from './risk-intelligence';
import { EmployeeService } from '../../services/employee';
import { RiskIntelligenceService } from '../../services/risk-intelligence';

const employee = { id: 7, name: 'Taylor Reed', department: 'Engineering', jobRole: 'Developer', age: 34 };
const summary = {
  employeeId: 7, attritionRisk: .72, performanceRisk: .4, promotionReadiness: .25, overallRisk: .62,
  promotionTargetType: 'portfolio_demo_rule_target',
  modelVersions: { attrition: 'attrition-v2', performance: 'performance-v1', promotionReadiness: 'rules-v1' },
  limitations: ['Not a calibrated probability', 'Synthetic training data']
};

describe('RiskIntelligence', () => {
  let component: RiskIntelligence;
  let fixture: ComponentFixture<RiskIntelligence>;
  let employeeService: { getEmployees: ReturnType<typeof vi.fn> };
  let riskService: { getSummary: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    employeeService = { getEmployees: vi.fn(() => of([employee])) };
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
  });

  it('loads employees and exposes accessible controls and the human-review disclaimer', () => {
    expect(component.employees).toEqual([employee]);
    expect(fixture.nativeElement.querySelector('#riskEmployee')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('button').getAttribute('aria-busy')).toBe('false');
    expect(fixture.nativeElement.textContent).toContain('Human review required');
  });

  it('shows an empty state and does not request a summary without employees', () => {
    employeeService.getEmployees.mockReturnValue(of([]));
    component.loadEmployees();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No employees are available');
    expect(riskService.getSummary).not.toHaveBeenCalled();
  });

  it('shows employee loading errors with a retry action', () => {
    employeeService.getEmployees.mockReturnValue(throwError(() => new Error('offline')));
    component.loadEmployees();
    fixture.detectChanges();
    expect(component.employeeErrorMessage).toContain('Unable to load employees');
    expect(fixture.nativeElement.textContent).toContain('Try again');
  });

  it('renders risk bands, score explanations, provenance, limitations, and disclaimer', () => {
    component.load();
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Moderate risk');
    expect(text).toContain('1 − class 4 performance score');
    expect(text).toContain('attrition-v2');
    expect(text).toContain('Synthetic training data');
    expect(text).toContain('Before deciding');
  });

  it('rejects an invalid summary response', () => {
    riskService.getSummary.mockReturnValue(of({ ...summary, overallRisk: 1.4 }));
    component.load();
    fixture.detectChanges();
    expect(component.summary).toBeNull();
    expect(component.invalidResponse).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('invalid summary');
  });

  it('shows service errors and a retry action', () => {
    riskService.getSummary.mockReturnValue(throwError(() => new Error('offline')));
    component.load();
    fixture.detectChanges();
    expect(component.summaryLoading).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Risk summary unavailable');
  });
});
