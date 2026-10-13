import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subject, of, throwError } from 'rxjs';
import { EmployeeProfile } from './employee-profile';
import { EmployeeService } from '../../services/employee';
import { EmployeeAiService } from '../../services/employee-ai.service';
import { RiskIntelligenceService } from '../../services/risk-intelligence';
import { AuditService } from '../../services/audit.service';

describe('EmployeeProfile', () => {
  let fixture: ComponentFixture<EmployeeProfile>;
  let component: EmployeeProfile;
  let employeeService: { getEmployeeById: ReturnType<typeof vi.fn> };
  let aiService: { assessRisk: ReturnType<typeof vi.fn> };
  let riskService: { getSummary: ReturnType<typeof vi.fn> };
  let auditService: { getEvents: ReturnType<typeof vi.fn> };

  const employee = { id: 7, name: 'Ada Lovelace', department: 'Research & Development', jobRole: 'Engineer', age: 35 };
  const risk = { employeeId: 7, prediction: 'Likely to stay', probability: 0.12, riskLevel: 'Low Risk' };
  const summary = { employeeId: 7, attritionRisk: 0.12, performanceRisk: 0.2, promotionReadiness: 0.8, overallRisk: 0.17, promotionTargetType: 'promotion', modelVersions: {}, limitations: ['Decision-support only'] };
  const auditPage = { content: [{ id: 1, timestamp: '2026-01-02T10:00:00Z', actorSubject: 'hr-admin', action: 'EMPLOYEE_VIEWED', resourceType: 'Employee', resourceId: '7', result: 'SUCCESS' }], page: 0, size: 100, totalElements: 1, totalPages: 1 };

  async function setup(employeeResult: Observable<typeof employee> = of(employee), riskResult: Observable<typeof risk> = of(risk), summaryResult: Observable<typeof summary> = of(summary), timelineResult = of(auditPage)) {
    employeeService = { getEmployeeById: vi.fn(() => employeeResult) };
    aiService = { assessRisk: vi.fn(() => riskResult) };
    riskService = { getSummary: vi.fn(() => summaryResult) };
    auditService = { getEvents: vi.fn(() => timelineResult) };
    await TestBed.configureTestingModule({
      imports: [EmployeeProfile],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '7' } } } },
        { provide: EmployeeService, useValue: employeeService },
        { provide: EmployeeAiService, useValue: aiService }
        ,{ provide: RiskIntelligenceService, useValue: riskService }
        ,{ provide: AuditService, useValue: auditService }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(EmployeeProfile);
    component = fixture.componentInstance;
  }

  it('renders profile sections and requests the employee risk assessment', async () => {
    await setup(); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Ada Lovelace');
    expect(fixture.nativeElement.textContent).toContain('Personal & employment');
    expect(fixture.nativeElement.textContent).toContain('Compensation & performance');
    expect(fixture.nativeElement.textContent).toContain('Employee 360 intelligence');
    expect(fixture.nativeElement.textContent).toContain('Employee 360 intelligence');
    expect(fixture.nativeElement.textContent).toContain('Overall risk');
    expect(fixture.nativeElement.textContent).toContain('Timeline & audit');
    expect(fixture.nativeElement.textContent).toContain('EMPLOYEE_VIEWED');
    expect(fixture.nativeElement.textContent).toContain('Decision-support only');
    expect(aiService.assessRisk).toHaveBeenCalledWith(7);
    expect(auditService.getEvents).toHaveBeenCalledWith('', '', 0, 100);
  });

  it('shows a loading state while the employee request is pending', async () => {
    const pending = new Subject<typeof employee>();
    await setup(pending.asObservable()); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Loading employee profile');
  });

  it('shows not found for a 404 employee response', async () => {
    await setup(throwError(() => ({ status: 404 }))); fixture.detectChanges();
    expect(component.notFound).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Employee not found');
  });

  it('shows a risk error while preserving the employee profile', async () => {
    await setup(of(employee), throwError(() => new Error('offline'))); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Ada Lovelace');
    expect(fixture.nativeElement.textContent).toContain('Risk assessment unavailable');
  });

  it('shows the employee profile when the 360 summary is unavailable', async () => {
    await setup(of(employee), of(risk), throwError(() => new Error('offline'))); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Ada Lovelace');
    expect(fixture.nativeElement.textContent).toContain('360 summary unavailable');
  });

  it('shows an empty timeline when no events belong to the employee', async () => {
    await setup(of(employee), of(risk), of(summary), of({ ...auditPage, content: [{ ...auditPage.content[0], resourceId: '99' }] })); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No timeline events yet');
    expect(fixture.nativeElement.textContent).not.toContain('EMPLOYEE_VIEWED');
  });

  it('shows a timeline error while preserving the profile', async () => {
    await setup(of(employee), of(risk), of(summary), throwError(() => new Error('offline'))); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Timeline unavailable');
    expect(fixture.nativeElement.textContent).toContain('Ada Lovelace');
  });

  it('shows a loading timeline while audit events are pending', async () => {
    const pending = new Subject<typeof auditPage>();
    await setup(of(employee), of(risk), of(summary), pending.asObservable()); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Loading timeline');
  });
});
