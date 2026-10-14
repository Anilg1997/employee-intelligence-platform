import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { OperationsService } from './operations.service';
import { apiUrl } from '../config/api.config';

describe('OperationsService API boundaries', () => {
  let api: OperationsService; let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] }); api = TestBed.inject(OperationsService); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('uses the model registry under the configured API prefix exactly once', () => { api.models().subscribe(); http.expectOne(apiUrl('/ml/models')).flush([]); });
  it('uses the Actuator root outside the API prefix', () => { api.health().subscribe(); http.expectOne('http://localhost:8080/actuator/health').flush({ status: 'UP' }); api.metricNames().subscribe(); http.expectOne('http://localhost:8080/actuator/metrics').flush({ names: [] }); });
  it('uses request parameters required by the existing search and ingestion controllers', () => {
    api.searchDocuments('leave & benefits').subscribe();
    const search = http.expectOne(r => r.url === apiUrl('/rag/documents/search'));
    expect(search.request.params.get('query')).toBe('leave & benefits'); expect(search.request.params.get('limit')).toBe('20'); search.flush([]);
    api.ingestDocument('documents/policy.md', 'Policy').subscribe();
    const ingest = http.expectOne(r => r.url === apiUrl('/rag/ingestion'));
    expect(ingest.request.method).toBe('POST'); expect(ingest.request.params.get('filePath')).toBe('documents/policy.md'); expect(ingest.request.params.get('source')).toBe('Policy'); ingest.flush({ message: 'Stored' });
  });
});
