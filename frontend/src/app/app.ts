import { Component, signal } from '@angular/core';
import {
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';
import { RolePolicyService } from './services/role-policy.service';
import { AuthService } from './services/auth.service';
import { NotificationPanel } from './components/notification-panel/notification-panel';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    NotificationPanel
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  constructor(readonly policy: RolePolicyService, readonly auth: AuthService) {}
  protected readonly navigationOpen = signal(false);

  protected toggleNavigation(): void {
    this.navigationOpen.update((open) => !open);
  }

  protected closeNavigation(): void {
    this.navigationOpen.set(false);
  }

  protected async signOut(): Promise<void> {
    await this.auth.signOut();
    this.closeNavigation();
  }
}
