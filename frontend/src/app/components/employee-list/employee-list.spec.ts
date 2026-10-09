import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { EmployeeList } from './employee-list';
import { EmployeeService } from '../../services/employee';
import { EmployeeAiService } from '../../services/employee-ai.service';

describe('EmployeeList', () => {
  let component: EmployeeList;
  let fixture: ComponentFixture<EmployeeList>;
  let employeeService: { searchEmployees: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    employeeService = { searchEmployees: vi.fn(() => of({
      content: [], page: 0, size: 20, totalElements: 0, totalPages: 0
    })) };
    await TestBed.configureTestingModule({
      imports: [EmployeeList],
      providers: [
        { provide: EmployeeService, useValue: employeeService },
        { provide: EmployeeAiService, useValue: { assessRisk: vi.fn() } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads the first page when filters are applied', () => {
    component.searchQuery = 'Ada';
    component.selectedDepartment = 'Sales';
    component.sortBy = 'age';
    component.sortDirection = 'DESC';
    component.pageSize = 10;
    component.currentPage = 3;

    component.applyFilters();

    expect(component.currentPage).toBe(0);
    expect(employeeService.searchEmployees).toHaveBeenLastCalledWith({
      query: 'Ada', department: 'Sales', page: 0, size: 10,
      sortBy: 'age', sortDirection: 'DESC'
    });
  });

  it('loads the selected page and keeps pagination metadata', () => {
    employeeService.searchEmployees.mockReturnValue(of({
      content: [{ id: 4, name: 'Ada', department: 'Sales', jobRole: 'Manager', age: 35 }],
      page: 1, size: 20, totalElements: 21, totalPages: 2
    }));

    component.totalPages = 2;
    component.changePage(1);

    expect(component.employees[0].name).toBe('Ada');
    expect(component.totalElements).toBe(21);
    expect(component.totalPages).toBe(2);
    expect(component.currentPage).toBe(1);
  });

  it('shows an error when the search fails', () => {
    employeeService.searchEmployees.mockReturnValue(throwError(() => new Error('offline')));

    component.loadEmployees();

    expect(component.errorMessage).toBe('Failed to load employees.');
    expect(component.loading).toBe(false);
  });
});
