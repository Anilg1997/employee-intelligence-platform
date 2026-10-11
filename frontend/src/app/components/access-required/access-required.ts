import { Component } from '@angular/core';

@Component({
  selector: 'app-access-required',
  standalone: true,
  template: `
    <section aria-labelledby="access-required-title">
      <h1 id="access-required-title">Access required</h1>
      <p>You need a valid authenticated session to access this application. Your session may have expired.</p>
      <p>Sign-in is not yet integrated in this frontend. Contact your administrator for access and deployment guidance.</p>
    </section>
  `
})
export class AccessRequired {}
