import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HrAssistant } from './hr-assistant';

describe('HrAssistant', () => {
  let component: HrAssistant;
  let fixture: ComponentFixture<HrAssistant>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HrAssistant],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(HrAssistant);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('returns a grounded answer and clears loading', () => {
    component.question = 'How do I request leave?';
    component.askQuestion();
    const request = http.expectOne(req => req.url.endsWith('/api/rag/ask'));
    expect(request.request.body).toEqual({ question: 'How do I request leave?' });
    request.flush({
      question: 'How do I request leave?',
      answer: 'Submit a leave request to your manager.',
      sources: [{ source: 'leave-policy.txt', similarity: 0.9, content: 'Submit a leave request to your manager.' }]
    });

    expect(component.loading).toBe(false);
    expect(component.response?.sources).toHaveLength(1);
  });

  it('clears loading and displays an error when the policy service fails', () => {
    component.question = 'How do I request leave?';
    component.askQuestion();
    http.expectOne(req => req.url.endsWith('/api/rag/ask'))
      .flush('Unavailable', { status: 503, statusText: 'Unavailable' });

    expect(component.loading).toBe(false);
    expect(component.errorMessage).toContain('Unable to get an answer');
  });
});
