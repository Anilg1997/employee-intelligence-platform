import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpTestingController } from '@angular/common/http/testing';

import { EmployeeService } from './employee';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(EmployeeService);
    http = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('builds search query parameters and maps the paginated response', () => {
    service.searchEmployees({
      query: '  Ada ',
      department: 'Sales',
      page: 2,
      size: 10,
      sortBy: 'age',
      sortDirection: 'DESC'
    }).subscribe();

    const request = http.expectOne(request => request.url.endsWith('/api/employees/search'));
    expect(request.request.params.get('query')).toBe('Ada');
    expect(request.request.params.get('department')).toBe('Sales');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.get('sortBy')).toBe('age');
    expect(request.request.params.get('sortDirection')).toBe('DESC');
    request.flush({ content: [], page: 2, size: 10, totalElements: 0, totalPages: 0 });
  });
});
