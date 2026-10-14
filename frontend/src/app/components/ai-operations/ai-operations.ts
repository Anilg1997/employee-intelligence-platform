import { Component, OnInit, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { OperationsService } from '../../services/operations.service';
import { ModelMetadata } from '../../models/operations';

@Component({ selector: 'app-ai-operations', standalone: true, imports: [CommonModule], templateUrl: './ai-operations.html', styleUrl: '../operations.scss' })
export class AiOperations implements OnInit {
  private readonly api = inject(OperationsService);
  private readonly destroy = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  models: ModelMetadata[] = []; loading = true; error = '';
  ngOnInit(): void { this.load(); }
  load(): void {
    this.loading = true; this.error = ''; this.models = [];
    this.api.models().pipe(takeUntilDestroyed(this.destroy)).subscribe({
      next: value => { if (Array.isArray(value)) this.models = value; else this.error = 'Model inventory returned an invalid response.'; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'AI model inventory is unavailable. No model status or metrics are being assumed.'; this.loading = false; this.cdr.markForCheck(); }
    });
  }
}
