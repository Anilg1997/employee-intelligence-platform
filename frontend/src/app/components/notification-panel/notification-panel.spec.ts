import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of } from 'rxjs';
import { signal } from '@angular/core';
import { NotificationPanel } from './notification-panel';
import { NotificationService } from '../../services/notification.service';
import { Notification } from '../../models/notification';
import { provideRouter } from '@angular/router';

const notification: Notification = { id: 4, type: 'risk.detected', title: 'Risk review available', body: 'Review the latest summary.', createdAt: '2026-01-01T00:00:00Z', readAt: null };

describe('NotificationPanel', () => {
  let fixture: ComponentFixture<NotificationPanel>;
  let stream: Subject<Notification>;
  let service: { streamState: ReturnType<typeof signal<'idle' | 'connected'>>; notifications$: Subject<Notification>; list: ReturnType<typeof vi.fn>; connect: ReturnType<typeof vi.fn>; disconnect: ReturnType<typeof vi.fn>; markAsRead: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    stream = new Subject<Notification>();
    service = { streamState: signal<'idle' | 'connected'>('connected'), notifications$: stream, list: vi.fn().mockReturnValue(of([notification])), connect: vi.fn(), disconnect: vi.fn(), markAsRead: vi.fn().mockReturnValue(of({ ...notification, readAt: '2026-01-01T01:00:00Z' })) };
    await TestBed.configureTestingModule({ imports: [NotificationPanel], providers: [provideRouter([]), { provide: NotificationService, useValue: service }] }).compileComponents();
    fixture = TestBed.createComponent(NotificationPanel);
    fixture.detectChanges();
  });

  it('loads recent notifications and exposes an unread count in the compact trigger', () => {
    expect(fixture.nativeElement.querySelector('.unread-badge')?.textContent).toContain('1');
    expect(service.list).toHaveBeenCalledWith(5);
    expect(service.connect).toHaveBeenCalled();
  });

  it('shows the latest SSE notification and marks it read from the panel', async () => {
    const newer = { ...notification, id: 5, title: 'Prediction complete', createdAt: '2026-01-02T00:00:00Z' };
    stream.next(newer);
    await fixture.whenStable();
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.notification-trigger').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Prediction complete');
    fixture.nativeElement.querySelector('.notification-item').click();
    expect(service.markAsRead).toHaveBeenCalledWith(5);
  });
});
