import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditService } from '../../services/audit.service';
import { AuditEvent } from '../../models/audit-event';
import { AUTH_ENABLED } from '../../config/auth.config';
import { finalize } from 'rxjs';
@Component({ selector: 'app-audit', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './audit.html', styleUrl: './audit.scss' })
export class Audit implements OnInit {
  private readonly api = inject(AuditService);
  private readonly cdr = inject(ChangeDetectorRef);
  events: AuditEvent[] = [];
  action = '';
  result = '';
  loading = false;
  error = '';
  total = 0;
  page = 0;
  totalPages = 0;
  readonly authEnabled = AUTH_ENABLED;

  ngOnInit(): void { this.load(); }

  load(): void {
    if (this.loading) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.api.getEvents(this.action.trim(), this.result, this.page)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: data => {
          this.events = data.content;
          this.total = data.totalElements;
          this.page = data.page;
          this.totalPages = data.totalPages;
        },
        error: () => {
          this.events = [];
          this.total = 0;
          this.totalPages = 0;
          this.error = 'Audit events are unavailable. Check the backend connection or your audit permissions.';
        }
      });
  }

  applyFilters(): void {
    this.page = 0;
    this.load();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.page || this.loading) {
      return;
    }

    this.page = page;
    this.load();
  }

  reset(): void {
    this.action = '';
    this.result = '';
    this.page = 0;
    this.load();
  }
}
