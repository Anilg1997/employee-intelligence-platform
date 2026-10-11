import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditService } from '../../services/audit.service';
import { AuditEvent } from '../../models/audit-event';
import { AUTH_ENABLED } from '../../config/auth.config';
@Component({ selector: 'app-audit', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './audit.html', styleUrl: './audit.scss' })
export class Audit implements OnInit {
  private readonly api = inject(AuditService); events: AuditEvent[] = []; action = ''; result = ''; loading = false; error = ''; total = 0; page = 0; readonly authEnabled = AUTH_ENABLED;
  ngOnInit(): void { this.load(); }
  load(): void { this.loading = true; this.error = ''; this.api.getEvents(this.action, this.result, this.page).subscribe({ next: data => { this.events = data.content; this.total = data.totalElements; this.loading = false; }, error: () => { this.error = 'Audit events are unavailable. Check the backend connection or your audit permissions.'; this.loading = false; } }); }
  reset(): void { this.action = ''; this.result = ''; this.page = 0; this.load(); }
}
