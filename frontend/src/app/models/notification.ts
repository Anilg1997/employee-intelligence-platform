export interface Notification {
  id: number;
  type: string;
  title: string;
  body: string;
  createdAt: string;
  readAt: string | null;
}

export type NotificationStreamState =
  | 'idle'
  | 'connecting'
  | 'connected'
  | 'reconnecting'
  | 'timeout'
  | 'error';
