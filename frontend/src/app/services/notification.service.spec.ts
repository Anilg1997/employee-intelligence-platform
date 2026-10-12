import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { NotificationService } from './notification.service';
import { AUTH_ENABLED_TOKEN } from '../config/auth.config';

describe('NotificationService', () => {
  let service: NotificationService;
  let http: HttpTestingController;
  let originalFetch: typeof window.fetch;

  beforeEach(() => {
    sessionStorage.setItem('oidc_access_token', `e.${btoa(JSON.stringify({ sub: 'subject-a', tenant_id: 'tenant-a', exp: Math.floor(Date.now() / 1000) + 3600 }))}.s`);
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), { provide: AUTH_ENABLED_TOKEN, useValue: true }] });
    service = TestBed.inject(NotificationService);
    http = TestBed.inject(HttpTestingController);
    originalFetch = window.fetch;
    localStorage.clear();
  });

  afterEach(() => {
    service.disconnect();
    http.verify();
    window.fetch = originalFetch;
    localStorage.clear();
    sessionStorage.clear();
  });

  it('lists notifications and marks a notification as read through HTTP', () => {
    service.list().subscribe(notifications => expect(notifications).toEqual([]));
    const listRequest = http.expectOne(request => request.url.endsWith('/api/notifications'));
    expect(listRequest.request.params.get('size')).toBe('20');
    listRequest.flush([]);
    service.markAsRead(7).subscribe(notification => expect(notification.readAt).toBeTruthy());
    const readRequest = http.expectOne(request => request.url.endsWith('/api/notifications/7/read'));
    expect(readRequest.request.method).toBe('POST');
    readRequest.flush({ id: 7, type: 'risk.detected', title: 'Risk review available', body: 'Review', createdAt: '2026-01-01T00:00:00Z', readAt: '2026-01-01T01:00:00Z' });
  });

  it('uses the bearer header and Last-Event-ID header without a token query parameter', async () => {
    const chunks = new TextEncoder().encode('id: 12\nevent: notification\ndata: {"id":12,"type":"risk.detected","title":"Risk review available","body":"Review","createdAt":"2026-01-01T00:00:00Z","readAt":null}\n\n');
    let captured: RequestInit | undefined;
    window.fetch = vi.fn((_input, init) => {
      captured = init;
      const stream = new ReadableStream<Uint8Array>({ start(controller) { controller.enqueue(chunks); } });
      return Promise.resolve(new Response(stream, { status: 200, headers: { 'Content-Type': 'text/event-stream' } }));
    }) as typeof window.fetch;
    const received: number[] = [];
    service.notifications$.subscribe(notification => received.push(notification.id));
    service.connect();
    await new Promise(resolve => setTimeout(resolve, 0));
    const headers = captured?.headers as Record<string, string>;
    expect(headers['Authorization']).toMatch(/^Bearer /);
    expect(headers['Last-Event-ID']).toBeUndefined();
    expect(String((window.fetch as ReturnType<typeof vi.fn>).mock.calls[0][0])).not.toContain('token');
    expect(received).toEqual([12]);
    expect(service.lastEventId()).toBe('12');
  });

  it('reconnects after a closed stream and preserves the cursor header', async () => {
    localStorage.setItem('employee-intelligence.notifications.last-event-id:tenant-a:subject-a', '12');
    const calls: RequestInit[] = [];
    window.fetch = vi.fn((_input, init) => {
      calls.push(init ?? {});
      return Promise.resolve(new Response(null, { status: 204 }));
    }) as typeof window.fetch;
    service.connect();
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(service.streamState()).toBe('error');
    service.disconnect();
    expect((calls[0].headers as Record<string, string>)['Last-Event-ID']).toBe('12');
  });
});
