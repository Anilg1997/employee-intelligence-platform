import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, ParamMap } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';
import { EmployeeProfile } from './employee-profile';

describe('EmployeeProfile', () => {
  let fixture: ComponentFixture<EmployeeProfile>;
  let component: EmployeeProfile;
  let http: HttpTestingController;
  let routeParams: BehaviorSubject<ParamMap>;

  beforeEach(async () => {
    routeParams = new BehaviorSubject(convertToParamMap({ id: '7' }));
    await TestBed.configureTestingModule({
      imports: [EmployeeProfile],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { paramMap: routeParams.asObservable() }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeProfile);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads and displays the requested employee profile', () => {
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Loading employee profile');

    const request = http.expectOne(request => request.url.endsWith('/api/employees/7'));
    request.flush({
      id: 7,
      name: 'Ada Lovelace',
      department: 'Research & Development',
      jobRole: 'Research Scientist',
      age: 35
    });
    fixture.detectChanges();

    expect(component.loading).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Ada Lovelace');
    expect(fixture.nativeElement.textContent).toContain('Research Scientist');
  });

  it('shows a not-found error for an unknown employee', () => {
    fixture.detectChanges();
    http.expectOne(request => request.url.endsWith('/api/employees/7')).flush(
      null,
      { status: 404, statusText: 'Not Found' }
    );
    fixture.detectChanges();

    expect(component.errorMessage).toBe('Employee not found.');
    expect(fixture.nativeElement.textContent).toContain('Employee not found.');
  });

  it('rejects an invalid employee ID without making an API request', async () => {
    routeParams.next(convertToParamMap({ id: 'invalid' }));
    fixture.detectChanges();

    expect(component.errorMessage).toBe('Invalid employee ID.');
    expect(component.loading).toBe(false);
  });
});
