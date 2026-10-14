import { Component, OnInit, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { OperationsService } from '../../services/operations.service';
import { ActuatorHealth } from '../../models/operations';
@Component({ selector: 'app-system-health', standalone: true, imports: [CommonModule], templateUrl: './system-health.html', styleUrl: '../operations.scss' })
export class SystemHealth implements OnInit {
  private readonly api = inject(OperationsService);
  private readonly destroy = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  health: ActuatorHealth | null = null; metrics: string[] = []; loading = true; error = '';
  metricData: Record<string, unknown> | null = null; metricLoading = false; metricError = ''; selectedMetric = '';
  ngOnInit(): void { this.load(); }
  load(): void {
    this.loading = true; this.error = ''; this.health = null; this.metrics = [];
    this.api.health().pipe(takeUntilDestroyed(this.destroy)).subscribe({
      next: h => { this.health = h; this.loading = false; this.cdr.markForCheck(); },
      error: err => { this.loading = false; this.health = err.status === 503 && typeof err.error?.status === 'string' ? err.error : null; this.error = this.health ? 'Actuator reports an unhealthy service.' : 'System health is unavailable. The Actuator endpoint could not be reached.'; this.cdr.markForCheck(); }
    });
    this.api.metricNames().pipe(takeUntilDestroyed(this.destroy)).subscribe({
      next: m => { this.metrics = m.names || []; this.metricError = ''; this.cdr.markForCheck(); },
      error: () => { this.metricError = 'Actuator metrics are unavailable or access is restricted.'; this.cdr.markForCheck(); }
    });
  }
  inspectMetric(name: string): void {
    this.selectedMetric = name; this.metricLoading = true; this.metricData = null; this.metricError = '';
    this.api.metric(name).pipe(takeUntilDestroyed(this.destroy)).subscribe({
      next: m => { this.metricData = m; this.metricLoading = false; this.cdr.markForCheck(); },
      error: () => { this.metricError = 'Metric measurements are unavailable or access is restricted.'; this.metricLoading = false; this.cdr.markForCheck(); }
    });
  }
  entries(): [string, { status?: string; details?: Record<string, unknown> }][] { return Object.entries(this.health?.components || {}); }
}
