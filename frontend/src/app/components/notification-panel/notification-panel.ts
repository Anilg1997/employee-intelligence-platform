import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { Notification } from '../../models/notification';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notification-panel',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './notification-panel.html',
  styleUrl: './notification-panel.scss'
})
export class NotificationPanel implements OnInit, OnDestroy {
  private readonly service = inject(NotificationService);
  private readonly subscription = new Subscription();

  notifications: Notification[] = [];
  open = false;
  loading = true;
  error = false;
  readonly unreadCount = signal(0);
  readonly streamState = this.service.streamState;

  ngOnInit(): void {
    this.subscription.add(this.service.notifications$.subscribe(notification => this.upsert(notification)));
    this.service.list(5).subscribe({
      next: notifications => { this.notifications = this.sort(notifications).slice(0, 5); this.unreadCount.set(this.notifications.filter(item => !item.readAt).length); this.loading = false; },
      error: () => { this.loading = false; this.error = true; }
    });
    this.service.connect();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
    this.service.disconnect();
  }

  toggle(): void { this.open = !this.open; }

  markAsRead(notification: Notification): void {
    if (notification.readAt) return;
    this.service.markAsRead(notification.id).subscribe({
      next: updated => this.upsert(updated),
      error: () => undefined
    });
  }

  private upsert(notification: Notification): void {
    const existing = this.notifications.findIndex(item => item.id === notification.id);
    const next = existing < 0
      ? [...this.notifications, notification]
      : this.notifications.map(item => item.id === notification.id ? notification : item);
    this.notifications = this.sort(next).slice(0, 5);
    this.unreadCount.set(this.notifications.filter(item => !item.readAt).length);
  }

  private sort(notifications: Notification[]): Notification[] {
    return [...notifications].sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt));
  }
}
