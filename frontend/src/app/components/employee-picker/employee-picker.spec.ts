import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmployeePicker } from './employee-picker';

describe('EmployeePicker', () => {
  let fixture: ComponentFixture<EmployeePicker>;
  let component: EmployeePicker;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeePicker],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeePicker);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads a bounded first page and emits a selected employee', async () => {
    fixture.detectChanges();
    await new Promise(resolve => setTimeout(resolve, 275));
    const request = http.expectOne(req =>
      req.url.endsWith('/api/employees/search') &&
      req.params.get('page') === '0' &&
      req.params.get('size') === '20'
    );
    request.flush({
      content: [{ id: 13, name: 'Employee 13', department: 'Sales', jobRole: 'Manager' }],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1
    });

    expect(component.loading).toBe(false);
    expect(component.employees).toHaveLength(1);
    component.select(13);
    expect(component.selectedEmployeeId).toBe(13);
  });

  it('clears the loading state and exposes a retry after failure', async () => {
    fixture.detectChanges();
    await new Promise(resolve => setTimeout(resolve, 275));
    http.expectOne(req => req.url.endsWith('/api/employees/search'))
      .flush('Unavailable', { status: 503, statusText: 'Unavailable' });

    expect(component.loading).toBe(false);
    expect(component.errorMessage).toContain('Unable to load employees');

    component.load();
    await new Promise(resolve => setTimeout(resolve, 275));
    http.expectOne(req => req.url.endsWith('/api/employees/search')).flush({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0
    });
    expect(component.errorMessage).toBe('');
  });
});
