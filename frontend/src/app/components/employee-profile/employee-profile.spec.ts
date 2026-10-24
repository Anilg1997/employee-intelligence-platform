import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, ParamMap } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';

import { Employee } from '../../models/employee';
import { EmployeeService } from '../../services/employee';
import { EmployeeAiService } from '../../services/employee-ai.service';
import { RiskIntelligenceService } from '../../services/risk-intelligence';
import { AuditService } from '../../services/audit.service';
import { EmployeeProfile } from './employee-profile';

describe('EmployeeProfile', () => {
  let fixture: ComponentFixture<EmployeeProfile>;
  let component: EmployeeProfile;
  let routeParams: BehaviorSubject<ParamMap>;
  let employeeService: { getEmployeeById: ReturnType<typeof vi.fn> };
  let aiService: { assessRisk: ReturnType<typeof vi.fn> };
  let riskService: { getSummary: ReturnType<typeof vi.fn> };
  let auditService: { getEvents: ReturnType<typeof vi.fn> };

  const employee: Employee = {
    id: 7,
    name: 'Ada Lovelace',
    department: 'Research & Development',
    jobRole: 'Engineer',
    age: 35,
    monthlyIncome: 8000,
    attrition: 'No'
  };
  const risk = {
    employeeId: 7,
    prediction: 'Likely to stay',
    probability: 0.12,
    riskLevel: 'Low Risk'
  };
  const summary = {
    employeeId: 7,
    attritionRisk: 0.12,
    performanceRisk: 0.2,
    promotionReadiness: 0.8,
    overallRisk: 0.17,
    promotionTargetType: 'promotion',
    modelVersions: {},
    limitations: ['Decision-support only']
  };
  const auditPage = {
    content: [{
      id: 1,
      timestamp: '2026-01-02T10:00:00Z',
      actorSubject: 'hr-admin',
      action: 'EMPLOYEE_VIEWED',
      resourceType: 'Employee',
      resourceId: '7',
      result: 'SUCCESS'
    }],
    page: 0,
    size: 100,
    totalElements: 1,
    totalPages: 1
  };

  beforeEach(async () => {
    routeParams = new BehaviorSubject(convertToParamMap({ id: '7' }));
    employeeService = { getEmployeeById: vi.fn(() => of(employee)) };
    aiService = { assessRisk: vi.fn(() => of(risk)) };
    riskService = { getSummary: vi.fn(() => of(summary)) };
    auditService = { getEvents: vi.fn(() => of(auditPage)) };
    await TestBed.configureTestingModule({
      imports: [EmployeeProfile],
      providers: [
        { provide: ActivatedRoute, useValue: { paramMap: routeParams.asObservable() } },
        { provide: EmployeeService, useValue: employeeService },
        { provide: EmployeeAiService, useValue: aiService },
        { provide: RiskIntelligenceService, useValue: riskService },
        { provide: AuditService, useValue: auditService }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(EmployeeProfile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads employee details, risk summaries, and associated audit history', () => {
    const text = fixture.nativeElement.textContent;
    expect(employeeService.getEmployeeById).toHaveBeenCalledWith(7);
    expect(aiService.assessRisk).toHaveBeenCalledWith(7);
    expect(riskService.getSummary).toHaveBeenCalledWith(7);
    expect(auditService.getEvents).toHaveBeenCalledWith('', '', 0, 100);
    expect(text).toContain('Ada Lovelace');
    expect(text).toContain('Personal Information');
    expect(text).toContain('Risk and decision support');
    expect(text).toContain('Overall risk');
    expect(text).toContain('Timeline & audit');
    expect(text).toContain('EMPLOYEE_VIEWED');
    expect(text).toContain('Decision-support only');
  });

  it('shows employee not-found errors', () => {
    employeeService.getEmployeeById.mockReturnValue(throwError(() => ({ status: 404 })));
    routeParams.next(convertToParamMap({ id: '8' }));
    fixture.detectChanges();
    expect(component.notFound).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Employee not found.');
  });

  it('rejects an invalid employee ID without calling the service', () => {
    employeeService.getEmployeeById.mockClear();
    routeParams.next(convertToParamMap({ id: 'invalid' }));
    fixture.detectChanges();
    expect(component.loading).toBe(false);
    expect(component.errorMessage).toBe('Invalid employee ID.');
    expect(employeeService.getEmployeeById).not.toHaveBeenCalled();
  });

  it('keeps the employee profile when the 360 summary is unavailable', () => {
    riskService.getSummary.mockReturnValue(throwError(() => new Error('offline')));
    routeParams.next(convertToParamMap({ id: '7' }));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Ada Lovelace');
    expect(fixture.nativeElement.textContent).toContain('360 summary unavailable');
  });

  it('filters unrelated audit events from the employee timeline', () => {
    auditService.getEvents.mockReturnValue(of({
      ...auditPage,
      content: [{ ...auditPage.content[0], resourceId: '99' }]
    }));
    routeParams.next(convertToParamMap({ id: '7' }));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No timeline events yet');
    expect(fixture.nativeElement.textContent).not.toContain('EMPLOYEE_VIEWED');
  });

  it('preserves the profile when timeline loading fails', () => {
    auditService.getEvents.mockReturnValue(throwError(() => new Error('offline')));
    routeParams.next(convertToParamMap({ id: '7' }));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Timeline unavailable');
    expect(fixture.nativeElement.textContent).toContain('Ada Lovelace');
  });
});
