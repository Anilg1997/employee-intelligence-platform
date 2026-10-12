import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, OnDestroy, inject, signal } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { apiUrl } from '../config/api.config';
import { AuthService, parseJwtPayload } from './auth.service';
import { Notification, NotificationStreamState } from '../models/notification';

const CURSOR_PREFIX = 'employee-intelligence.notifications.last-event-id';
export const SSE_CONNECTION_TIMEOUT_MS = 15_000;
export const SSE_RECONNECT_DELAYS_MS = [1_000, 2_000, 5_000, 10_000, 30_000] as const;

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly notificationSubject = new Subject<Notification>();
  private abortController: AbortController | null = null;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private reconnectAttempt = 0;
  private manuallyStopped = false;
  private sessionRevision = -1;

  readonly streamState = signal<NotificationStreamState>('idle');
  readonly lastEventId = signal(this.readLastEventId());
  readonly notifications$ = this.notificationSubject.asObservable();

  list(limit = 20): Observable<Notification[]> {
    const params = new HttpParams().set('size', String(limit));
    return this.http.get<Notification[]>(apiUrl('notifications'), { params });
  }

  markAsRead(id: number): Observable<Notification> {
    return this.http.post<Notification>(apiUrl(`notifications/${id}/read`), {});
  }

  connect(): void {
    if (this.abortController || this.reconnectTimer) return;
    this.manuallyStopped = false;
    if (!this.auth.isAuthenticated()) { this.streamState.set('error'); return; }
    this.sessionRevision = this.auth.sessionRevision();
    this.lastEventId.set(this.readLastEventId());
    this.openStream();
  }

  disconnect(): void {
    this.manuallyStopped = true;
    if (this.reconnectTimer) { clearTimeout(this.reconnectTimer); this.reconnectTimer = null; }
    this.abortController?.abort();
    this.abortController = null;
    this.reconnectAttempt = 0;
    this.streamState.set('idle');
  }

  ngOnDestroy(): void { this.disconnect(); this.notificationSubject.complete(); }

  private openStream(): void {
    if (this.manuallyStopped || typeof window === 'undefined' || !window.fetch) return;
    const token = this.auth.token();
    if (!token || this.auth.sessionRevision() !== this.sessionRevision) { this.streamState.set('error'); return; }
    const controller = new AbortController();
    this.abortController = controller;
    this.streamState.set(this.reconnectAttempt > 0 ? 'reconnecting' : 'connecting');
    const timeout = setTimeout(() => controller.abort(), SSE_CONNECTION_TIMEOUT_MS);
    const headers: Record<string, string> = { Accept: 'text/event-stream', Authorization: `Bearer ${token}` };
    const cursor = this.lastEventId();
    if (cursor) headers['Last-Event-ID'] = cursor;

    window.fetch(apiUrl('notifications/stream'), { method: 'GET', headers, signal: controller.signal, credentials: 'same-origin' })
      .then(response => {
        clearTimeout(timeout);
        if (!response.ok || !response.body) throw new Error(`SSE ${response.status}`);
        if (controller !== this.abortController) return;
        this.reconnectAttempt = 0;
        this.streamState.set('connected');
        return this.readStream(response.body.getReader(), controller);
      })
      .catch(error => {
        clearTimeout(timeout);
        if (controller !== this.abortController || this.manuallyStopped) return;
        this.abortController = null;
        this.streamState.set(error?.name === 'AbortError' ? 'timeout' : 'error');
        this.scheduleReconnect();
      });
  }

  private async readStream(reader: ReadableStreamDefaultReader<Uint8Array>, controller: AbortController): Promise<void> {
    const decoder = new TextDecoder();
    let buffer = '';
    let eventName = '';
    let eventId = '';
    let data: string[] = [];
    const dispatch = (): void => {
      if (!data.length) { eventName = ''; eventId = ''; return; }
      const currentId = eventId;
      if (currentId && this.isPreviouslyHandled(currentId)) { data = []; eventName = ''; eventId = ''; return; }
      if (currentId) this.persistLastEventId(currentId);
      if (eventName === 'notification' || !eventName) {
        try {
          const notification = JSON.parse(data.join('\n')) as Notification;
          if (notification && typeof notification.id === 'number') this.notificationSubject.next(notification);
        } catch { /* Ignore keepalives or malformed untrusted event data. */ }
      }
      data = []; eventName = ''; eventId = '';
    };
    try {
      while (true) {
        const result = await reader.read();
        if (result.done) throw new Error('SSE connection closed');
        buffer += decoder.decode(result.value, { stream: true });
        const lines = buffer.split(/\r?\n/);
        buffer = lines.pop() ?? '';
        for (const line of lines) {
          if (!line) { dispatch(); continue; }
          if (line.startsWith(':')) continue;
          const separator = line.indexOf(':');
          const field = separator < 0 ? line : line.slice(0, separator);
          const value = separator < 0 ? '' : line.slice(separator + 1).replace(/^ /, '');
          if (field === 'event') eventName = value;
          else if (field === 'id') eventId = value;
          else if (field === 'data') data.push(value);
        }
      }
    } finally {
      reader.releaseLock();
      if (controller === this.abortController && !this.manuallyStopped) {
        this.abortController = null;
        this.streamState.set('error');
        this.scheduleReconnect();
      }
    }
  }

  private scheduleReconnect(): void {
    if (this.manuallyStopped || this.reconnectTimer) return;
    const delay = SSE_RECONNECT_DELAYS_MS[Math.min(this.reconnectAttempt, SSE_RECONNECT_DELAYS_MS.length - 1)];
    this.reconnectAttempt += 1;
    this.reconnectTimer = setTimeout(() => { this.reconnectTimer = null; this.openStream(); }, delay);
  }

  private cursorKey(): string {
    const payload = this.auth.token() ? parseJwtPayload(this.auth.token()!) : null;
    const subject = typeof payload?.['sub'] === 'string' ? payload['sub'] : 'anonymous';
    const tenant = ['tenant_id', 'organization', 'tenant'].map(key => payload?.[key]).find(value => typeof value === 'string') ?? 'unknown';
    return `${CURSOR_PREFIX}:${tenant}:${subject}`;
  }
  private persistLastEventId(id: string): void { this.lastEventId.set(id); try { localStorage.setItem(this.cursorKey(), id); } catch { } }
  private readLastEventId(): string | null { try { return typeof localStorage === 'undefined' ? null : localStorage.getItem(this.cursorKey()); } catch { return null; } }
  private isPreviouslyHandled(id: string): boolean {
    const previous = this.lastEventId();
    if (!previous) return false;
    if (/^\d+$/.test(previous) && /^\d+$/.test(id)) return BigInt(id) <= BigInt(previous);
    return id === previous;
  }
}
