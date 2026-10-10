import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subject, of, throwError } from 'rxjs';
import { EmployeeProfile } from './employee-profile';
import { EmployeeService } from '../../services/employee';
import { EmployeeAiService } from '../../services/employee-ai.service';

describe('EmployeeProfile', () => {
  let fixture: ComponentFixture<EmployeeProfile>;
  let component: EmployeeProfile;
  let employeeService: { getEmployeeById: ReturnType<typeof vi.fn> };
  let aiService: { assessRisk: ReturnType<typeof vi.fn> };

  const employee = { id: 7, name: 'Ada Lovelace', department: 'Research & Development', jobRole: 'Engineer', age: 35 };
  const risk = { employeeId: 7, prediction: 'Likely to stay', probability: 0.12, riskLevel: 'Low Risk' };

  async function setup(employeeResult: Observable<typeof employee> = of(employee), riskResult: Observable<typeof risk> = of(risk)) {
    employeeService = { getEmployeeById: vi.fn(() => employeeResult) };
    aiService = { assessRisk: vi.fn(() => riskResult) };
    await TestBed.configureTestingModule({
      imports: [EmployeeProfile],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '7' } } } },
        { provide: EmployeeService, useValue: employeeService },
        { provide: EmployeeAiService, useValue: aiService }
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
    expect(fixture.nativeElement.textContent).toContain('ML intelligence');
    expect(fixture.nativeElement.textContent).toContain('Timeline & audit');
    expect(fixture.nativeElement.textContent).toContain('Not yet available');
    expect(aiService.assessRisk).toHaveBeenCalledWith(7);
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
});
