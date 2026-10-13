import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeeForm } from './employee-form';
import { EmployeeService } from '../../services/employee';
import { EmployeeAiService } from '../../services/employee-ai.service';

describe('EmployeeForm', () => {
  let component: EmployeeForm;
  let fixture: ComponentFixture<EmployeeForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeForm],
      providers: [
        { provide: EmployeeService, useValue: {} },
        { provide: EmployeeAiService, useValue: {} }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders the complete fictional department list', () => {
    fixture.detectChanges();

    const options = Array.from(fixture.nativeElement.querySelectorAll('#department option'))
      .map(option => (option as HTMLOptionElement).textContent?.trim());

    expect(options).toEqual(['Select a department', 'Human Resources', 'Research & Development', 'Sales']);
  });
});
