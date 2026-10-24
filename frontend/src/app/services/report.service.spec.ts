import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ReportService } from './report.service';

describe('ReportService', () => {
  let service: ReportService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [ReportService, provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ReportService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('uses the reports API contract for listing and generating', () => {
    service.list().subscribe(reports => expect(reports).toHaveLength(1));
    http.expectOne(request => request.url.endsWith('/api/reports')).flush({ content: [{ id: 1 }] });
    service.generate({ reportType: 'WORKFORCE' }).subscribe();
    const request = http.expectOne(item => item.url.endsWith('/api/reports'));
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ reportType: 'WORKFORCE' });
    request.flush({ id: 2 });
  });
});
