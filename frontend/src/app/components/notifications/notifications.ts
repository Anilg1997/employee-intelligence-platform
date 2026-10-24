import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { Notification, NotificationStreamState } from '../../models/notification';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './notifications.html',
  styleUrl: './notifications.scss'
})
export class Notifications implements OnInit, OnDestroy {
  private readonly service = inject(NotificationService);
  private readonly streamSubscription = new Subscription();

  notifications: Notification[] = [];
  loading = true;
  errorMessage = '';
  readError = '';
  readonly streamState = this.service.streamState;
  private readonly markingRead = new Set<number>();

  ngOnInit(): void {
    this.streamSubscription.add(this.service.notifications$.subscribe(notification => this.addNotification(notification)));
    this.load();
    this.service.connect();
  }

  ngOnDestroy(): void {
    this.streamSubscription.unsubscribe();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.service.list().subscribe({
      next: notifications => {
        this.notifications = this.sortNotifications(notifications);
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Notifications are unavailable. Check the backend connection and try again.';
        this.loading = false;
      }
    });
  }

  markAsRead(notification: Notification): void {
    if (notification.readAt || this.markingRead.has(notification.id)) return;
    this.readError = '';
    this.markingRead.add(notification.id);
    this.service.markAsRead(notification.id).subscribe({
      next: updated => {
        this.replaceNotification(updated);
        this.markingRead.delete(notification.id);
      },
      error: () => {
        this.markingRead.delete(notification.id);
        this.readError = 'That notification could not be marked as read. Please try again.';
      }
    });
  }

  isMarkingRead(notification: Notification): boolean {
    return this.markingRead.has(notification.id);
  }

  stateLabel(state: NotificationStreamState): string {
    switch (state) {
      case 'connected': return 'Live updates connected';
      case 'connecting': return 'Connecting to live updates…';
      case 'reconnecting': return 'Live updates disconnected. Reconnecting…';
      case 'timeout': return 'Live updates timed out. Reconnecting…';
      case 'error': return 'Live updates are unavailable. Reconnecting…';
      default: return '';
    }
  }

  private addNotification(notification: Notification): void {
    this.replaceNotification(notification);
  }

  private replaceNotification(notification: Notification): void {
    const index = this.notifications.findIndex(item => item.id === notification.id);
    if (index === -1) this.notifications = this.sortNotifications([...this.notifications, notification]);
    else this.notifications = this.sortNotifications(this.notifications.map(item => item.id === notification.id ? notification : item));
  }

  private sortNotifications(notifications: Notification[]): Notification[] {
    return [...notifications].sort((first, second) => Date.parse(second.createdAt) - Date.parse(first.createdAt));
  }
}
