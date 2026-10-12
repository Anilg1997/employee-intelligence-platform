import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-access-required',
  standalone: true,
  template: `
    <section aria-labelledby="access-required-title">
      <h1 id="access-required-title">Access required</h1>
      <p>You need a valid authenticated session to access this application. Your session may have expired.</p>
      @if (auth.oidcConfigured()) {
        <button type="button" (click)="signIn()" [disabled]="busy()">{{ busy() ? 'Opening sign-in…' : 'Sign in' }}</button>
        @if (error()) { <p role="alert">{{ error() }}</p> }
      } @else {
        <p>Sign-in is not configured for this deployment. Contact the administrator for the OIDC issuer, public client, and redirect settings.</p>
      }
    </section>
  `
})
export class AccessRequired {
  readonly auth = inject(AuthService);
  readonly busy = signal(false);
  readonly error = signal('');

  async signIn(): Promise<void> {
    this.busy.set(true); this.error.set('');
    try { await this.auth.signIn(); }
    catch { this.busy.set(false); this.error.set('Sign-in could not be started. Check the identity configuration.'); }
  }
}
