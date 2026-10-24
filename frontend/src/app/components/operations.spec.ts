import { TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';
import { OperationsService } from '../services/operations.service';
import { AiOperations } from './ai-operations/ai-operations';
import { SystemHealth } from './system-health/system-health';
import { Knowledge } from './knowledge/knowledge';
import { Administration } from './administration/administration';

describe('Enterprise operations screens', () => {
  let api: { models: ReturnType<typeof vi.fn>; health: ReturnType<typeof vi.fn>; metricNames: ReturnType<typeof vi.fn>; metric: ReturnType<typeof vi.fn>; searchDocuments: ReturnType<typeof vi.fn>; ingestDocument: ReturnType<typeof vi.fn> };
  beforeEach(() => {
    api = { models: vi.fn(() => of([])), health: vi.fn(() => of({ status: 'UP' })), metricNames: vi.fn(() => of({ names: [] })), metric: vi.fn(() => of({})), searchDocuments: vi.fn(() => of([])), ingestDocument: vi.fn(() => of({ message: 'Ingested' })) };
    TestBed.configureTestingModule({ providers: [{ provide: OperationsService, useValue: api }] });
  });
  it('shows model loading, nested metrics and limitations from asynchronous data', async () => {
    const response = new Subject<unknown>(); api.models.mockReturnValue(response);
    const fixture = TestBed.createComponent(AiOperations); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Loading model inventory');
    response.next([{ name: 'salary', version: 'v1', metrics: { test: { r2: .7 } }, limitations: ['Synthetic data'] }]);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('r2'); expect(fixture.nativeElement.textContent).toContain('Synthetic data');
  });
  it('does not display stale model data after an unavailable refresh', () => {
    api.models.mockReturnValue(of([{ name: 'salary', version: 'v1' }])); const fixture = TestBed.createComponent(AiOperations); fixture.detectChanges();
    api.models.mockReturnValue(throwError(() => ({ status: 503 }))); fixture.componentInstance.load(); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy(); expect(fixture.componentInstance.models).toEqual([]);
  });
  it('retains a real DOWN health payload on HTTP 503 and independently loads metrics', () => {
    api.health.mockReturnValue(throwError(() => ({ status: 503, error: { status: 'DOWN' } })));
    api.metricNames.mockReturnValue(of({ names: ['jvm.memory.used'] }));
    const fixture = TestBed.createComponent(SystemHealth); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('DOWN'); expect(fixture.nativeElement.textContent).toContain('jvm.memory.used');
    api.metric.mockReturnValue(throwError(() => ({ status: 403 }))); fixture.componentInstance.inspectMetric('jvm.memory.used'); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('access is restricted');
  });
  it('labels missing inventory and only shows an empty search after submission', () => {
    const fixture = TestBed.createComponent(Knowledge); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('full document inventory is unavailable');
    expect(fixture.nativeElement.textContent).not.toContain('No matching');
    fixture.componentInstance.query = 'policy'; fixture.componentInstance.search(); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No matching');
  });
  it('validates ingestion and prevents duplicate requests while pending', () => {
    const fixture = TestBed.createComponent(Knowledge); const c = fixture.componentInstance;
    c.ingest(); expect(api.ingestDocument).not.toHaveBeenCalled();
    const pending = new Subject<unknown>(); api.ingestDocument.mockReturnValue(pending);
    c.filePath = ' policy.md '; c.source = ' HR '; c.ingest(); c.ingest();
    expect(api.ingestDocument).toHaveBeenCalledTimes(1); expect(api.ingestDocument).toHaveBeenCalledWith('policy.md', 'HR');
    pending.next({ message: 'Document ingested successfully' }); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Document ingested successfully');
  });
  it('renders administration as read-only without tokens or mutation controls', () => {
    const fixture = TestBed.createComponent(Administration); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Read-only configuration');
    expect(fixture.nativeElement.querySelector('button, input, select')).toBeNull();
  });
});
