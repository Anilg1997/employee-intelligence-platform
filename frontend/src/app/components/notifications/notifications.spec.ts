import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, Subject, of, throwError } from 'rxjs';
import { signal } from '@angular/core';
import { Notifications } from './notifications';
import { Notification } from '../../models/notification';
import { NotificationService } from '../../services/notification.service';

const unread: Notification = {
  id: 1,
  type: 'risk.detected',
  title: 'Risk review available',
  body: 'Review the latest risk summary.',
  createdAt: '2026-01-01T00:00:00Z',
  readAt: null
};

describe('Notifications', () => {
  let fixture: ComponentFixture<Notifications>;
  let component: Notifications;
  let stream: Subject<Notification>;
  let service: {
    streamState: ReturnType<typeof signal<'idle' | 'connected'>>;
    notifications$: Observable<Notification>;
    list: ReturnType<typeof vi.fn>;
    markAsRead: ReturnType<typeof vi.fn>;
    connect: ReturnType<typeof vi.fn>;
    disconnect: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    stream = new Subject<Notification>();
    service = {
      streamState: signal<'idle' | 'connected'>('connected'),
      notifications$: stream.asObservable(),
      list: vi.fn().mockReturnValue(of([unread])),
      markAsRead: vi.fn().mockReturnValue(of({ ...unread, readAt: '2026-01-01T01:00:00Z' })),
      connect: vi.fn(),
      disconnect: vi.fn()
    };
    await TestBed.configureTestingModule({
      imports: [Notifications],
      providers: [{ provide: NotificationService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(Notifications);
    component = fixture.componentInstance;
  });

  it('renders unread notifications and marks them read with an accessible action', () => {
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('.read-button') as HTMLButtonElement;
    expect(fixture.nativeElement.textContent).toContain('Unread');
    expect(button.getAttribute('aria-label')).toContain('Mark Risk review available as read');

    button.click();
    fixture.detectChanges();
    expect(service.markAsRead).toHaveBeenCalledWith(1);
    expect(fixture.nativeElement.textContent).toContain('Read');
  });

  it('shows an empty state after a successful empty response', () => {
    service.list.mockReturnValue(of([]));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('You’re all caught up');
  });

  it('shows a retryable error when the list request fails', () => {
    service.list.mockReturnValue(throwError(() => new Error('offline')));
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Notifications are unavailable');
  });
});
