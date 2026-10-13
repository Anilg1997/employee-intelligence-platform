import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { Dashboard } from './dashboard';

describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('shows a loading state while the summary is pending', () => {
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Loading workforce snapshot');
    expect(component.loading).toBe(true);
    http.expectOne(request => request.url.endsWith('/api/dashboard/summary')).flush({
      totalEmployees: 0,
      departmentStatistics: {}
    });
  });

  it('renders summary metrics and department rows from the API response', async () => {
    fixture.detectChanges();
    const request = http.expectOne(request => request.url.endsWith('/api/dashboard/summary'));

    request.flush({
      totalEmployees: 12,
      departmentStatistics: { Engineering: 7, People: 5 }
    });
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(component.dashboard?.totalEmployees).toBe(12);
    expect(component.departmentSummaries[0].name).toBe('Engineering');
    expect(component.departmentSummaries[0].share).toBeCloseTo(58.3, 1);
    expect(component.loading).toBe(false);
  });

  it('derives an honest attrition distribution and leaves unsupported performance data empty', async () => {
    fixture.detectChanges();
    http.expectOne(request => request.url.endsWith('/api/dashboard/summary')).flush({
      totalEmployees: 10,
      departmentStatistics: { Engineering: 10 },
      attritionLabeledEmployees: 6,
      attritionCount: 2,
      attritionRate: 2 / 6
    });
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(component.attritionRetainedEmployees).toBe(4);
    expect(component.attritionUnlabeledEmployees).toBe(4);
    expect(component.hasPerformanceData).toBe(false);
  });

  it('renders performance distribution only when the response supplies it', async () => {
    fixture.detectChanges();
    http.expectOne(request => request.url.endsWith('/api/dashboard/summary')).flush({
      totalEmployees: 3,
      departmentStatistics: { Sales: 3 },
      performanceDistribution: { '3': 2, '4': 1 }
    });
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(component.hasPerformanceData).toBe(true);
    expect(component.performanceSummaries[0]).toMatchObject({ name: '3', count: 2 });
  });

  it('shows an error state when the summary request fails', async () => {
    fixture.detectChanges();
    const request = http.expectOne(request => request.url.endsWith('/api/dashboard/summary'));

    request.flush('Unavailable', {
      status: 503,
      statusText: 'Service Unavailable'
    });
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(component.loading).toBe(false);
    expect(component.errorMessage).toBe('Failed to load dashboard.');
  });
});
