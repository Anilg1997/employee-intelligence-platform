import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Audit } from './audit';

describe('Audit', () => {
  let fixture: ComponentFixture<Audit>;
  let component: Audit;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Audit],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(Audit);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads events and requests the next page', () => {
    fixture.detectChanges();
    http.expectOne(request => request.url.endsWith('/api/audit/events')).flush({
      content: [{ id: 1, timestamp: '2026-10-24T10:00:00Z', actorSubject: 'hr', action: 'READ', result: 'SUCCESS' }],
      page: 0,
      size: 25,
      totalElements: 26,
      totalPages: 2
    });

    expect(component.events).toHaveLength(1);
    expect(component.totalPages).toBe(2);

    component.goToPage(1);
    const nextPage = http.expectOne(request =>
      request.url.endsWith('/api/audit/events') &&
      request.params.get('page') === '1'
    );
    nextPage.flush({
      content: [],
      page: 1,
      size: 25,
      totalElements: 26,
      totalPages: 2
    });

    expect(component.page).toBe(1);
  });

  it('resets filtered searches to the first page and exposes retryable errors', () => {
    fixture.detectChanges();
    http.expectOne(request => request.url.endsWith('/api/audit/events')).flush({
      content: [],
      page: 0,
      size: 25,
      totalElements: 0,
      totalPages: 0
    });

    component.page = 2;
    component.action = ' ML_PREDICTION ';
    component.result = 'FAILURE';
    component.applyFilters();

    const filtered = http.expectOne(request =>
      request.url.endsWith('/api/audit/events') &&
      request.params.get('page') === '0' &&
      request.params.get('action') === 'ML_PREDICTION' &&
      request.params.get('result') === 'FAILURE'
    );
    filtered.flush('Unavailable', { status: 503, statusText: 'Unavailable' });

    expect(component.error).toContain('Audit events are unavailable');
    expect(component.loading).toBe(false);

    component.load();
    http.expectOne(request => request.url.endsWith('/api/audit/events')).flush({
      content: [],
      page: 0,
      size: 25,
      totalElements: 0,
      totalPages: 0
    });
    expect(component.error).toBe('');
  });
});
