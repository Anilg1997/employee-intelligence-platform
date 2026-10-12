import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-auth-callback',
  standalone: true,
  template: `<section aria-live="polite"><h1>Completing sign-in</h1><p>{{ message }}</p></section>`
})
export class AuthCallback implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  message = 'Verifying your session…';

  async ngOnInit(): Promise<void> {
    try {
      await this.auth.completeSignIn();
      await this.router.navigateByUrl('/dashboard');
    } catch {
      this.message = 'Sign-in could not be completed. Return to the access page and try again.';
      await this.router.navigateByUrl('/access-required');
    }
  }
}
