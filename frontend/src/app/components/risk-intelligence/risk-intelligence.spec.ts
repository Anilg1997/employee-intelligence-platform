import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Employee } from '../../models/employee';
import { RiskIntelligence } from './risk-intelligence';

describe('RiskIntelligence', () => {
  let fixture: ComponentFixture<RiskIntelligence>;
  let component: RiskIntelligence;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RiskIntelligence],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(RiskIntelligence);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads the real risk response and clears loading', async () => {
    fixture.detectChanges();
    await new Promise(resolve => setTimeout(resolve, 275));
    http.expectOne(request => request.url.endsWith('/api/employees/search'))
      .flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });

    const employee: Employee = {
      id: 13,
      name: 'Employee 13',
      department: 'Sales',
      jobRole: 'Healthcare Representative'
    };
    component.onEmployeeSelected(employee);
    component.load();

    http.expectOne('http://localhost:8080/api/ai/employees/13/risk-summary').flush({
      employeeId: 13,
      attritionRisk: 0.2,
      performanceRisk: 0.3,
      promotionReadiness: 0.5,
      overallRisk: 0.33,
      promotionTargetType: 'portfolio_demo_rule_target',
      modelVersions: { attrition: 'current', performance: 'v1', promotionReadiness: 'v1' },
      limitations: ['Decision-support only']
    });

    expect(component.loading).toBe(false);
    expect(component.summary?.employeeId).toBe(13);
  });

  it('clears loading and offers a meaningful error on failure', async () => {
    fixture.detectChanges();
    await new Promise(resolve => setTimeout(resolve, 275));
    http.expectOne(request => request.url.endsWith('/api/employees/search'))
      .flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
    component.onEmployeeSelected({
      id: 13,
      name: 'Employee 13',
      department: 'Sales',
      jobRole: 'Healthcare Representative'
    });
    component.load();
    http.expectOne(request => request.url.endsWith('/api/ai/employees/13/risk-summary'))
      .flush('Unavailable', { status: 503, statusText: 'Unavailable' });

    expect(component.loading).toBe(false);
    expect(component.errorMessage).toContain('Risk summary is unavailable');
  });
});
