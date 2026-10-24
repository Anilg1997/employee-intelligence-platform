import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { Dashboard } from './dashboard';

describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('shows a loading state while the summary is pending', () => {
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Loading workforce data');
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

  it('normalizes department names and updates charts when filtering', () => {
    fixture.detectChanges();
    http.expectOne(request => request.url.endsWith('/api/dashboard/summary')).flush({
      totalEmployees: 10,
      departmentStatistics: {
        Sales: 2,
        ' sales ': 3,
        'Research & Development': 5
      }
    });

    expect(component.departmentSummaries).toEqual([
      { name: 'Research & Development', count: 5, share: 50 },
      { name: 'Sales', count: 5, share: 50 }
    ]);

    component.toggleDepartmentFilter('Sales');

    expect(component.visibleDepartmentSummaries).toEqual([
      { name: 'Sales', count: 5, share: 50 }
    ]);
    expect(component.doughnutData.labels).toEqual(['Sales']);
    expect(component.barData.datasets[0].data).toEqual([5]);

    component.clearDepartmentFilter();
    expect(component.doughnutData.labels).toEqual([
      'Research & Development',
      'Sales'
    ]);
  });

  it('handles a zero-workforce response and refreshes from the API', () => {
    fixture.detectChanges();
    http.expectOne(request => request.url.endsWith('/api/dashboard/summary')).flush({
      totalEmployees: 0,
      departmentStatistics: {}
    });

    expect(component.hasDepartmentData).toBe(false);
    expect(component.averageDepartmentSize).toBe(0);
    expect(component.departmentSummaries).toEqual([]);

    component.loadDashboard();
    http.expectOne(request => request.url.endsWith('/api/dashboard/summary')).flush({
      totalEmployees: 2,
      departmentStatistics: { Sales: 2 }
    });

    expect(component.dashboard?.totalEmployees).toBe(2);
    expect(component.departmentSummaries[0].share).toBe(100);
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
    expect(component.errorMessage).toContain('Could not load workforce data.');
  });
});
