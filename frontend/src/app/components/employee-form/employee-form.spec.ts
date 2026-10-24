import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { EmployeeForm } from './employee-form';
import { EmployeeService } from '../../services/employee';
import { EmployeeAiService } from '../../services/employee-ai.service';
import { RolePolicyService } from '../../services/role-policy.service';

describe('EmployeeForm', () => {
  let component: EmployeeForm;
  let fixture: ComponentFixture<EmployeeForm>;
  let employeeService: { createEmployee: ReturnType<typeof vi.fn> };
  let employeeAiService: { predictEmployee: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    employeeService = { createEmployee: vi.fn() };
    employeeAiService = { predictEmployee: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [EmployeeForm],
      providers: [
        { provide: EmployeeService, useValue: employeeService },
        { provide: EmployeeAiService, useValue: employeeAiService },
        {
          provide: RolePolicyService,
          useValue: { canMutateEmployees: () => true }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not invent demographics or submit an incomplete employee', () => {
    expect(component.employee.age).toBeNull();
    expect(component.employee.attrition).toBeUndefined();
    expect(component.employee.businessTravel).toBeUndefined();
    expect(component.employee.gender).toBeUndefined();

    component.createEmployee();

    expect(component.errorMessage).toContain('Enter a name');
    expect(employeeService.createEmployee).not.toHaveBeenCalled();
  });

  it('creates an employee but skips prediction when model features are missing', () => {
    const created = {
      id: 7,
      name: 'Ada Lovelace',
      department: 'Research & Development',
      jobRole: 'Research Scientist',
      age: 35
    };
    employeeService.createEmployee.mockReturnValue(of(created));
    component.employee = {
      name: created.name,
      department: created.department,
      jobRole: created.jobRole,
      age: created.age
    };

    component.createEmployee();

    expect(employeeService.createEmployee).toHaveBeenCalledOnce();
    expect(employeeAiService.predictEmployee).not.toHaveBeenCalled();
    expect(component.predictionNotice).toContain('prediction was skipped');
    expect(component.employee.age).toBeNull();
  });
});
