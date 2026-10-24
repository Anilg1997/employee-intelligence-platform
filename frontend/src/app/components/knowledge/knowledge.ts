import { Component, inject, ChangeDetectorRef, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { OperationsService } from '../../services/operations.service';
import { DocumentSearchResult } from '../../models/operations';
@Component({ selector: 'app-knowledge', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './knowledge.html', styleUrl: '../operations.scss' })
export class Knowledge {
  private readonly api = inject(OperationsService);
  private readonly destroy = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  query = ''; results: DocumentSearchResult[] = []; loading = false; searched = false;
  ingesting = false; error = ''; ingestionError = ''; message = ''; filePath = ''; source = '';
  search(): void {
    if (this.loading) return;
    if (!this.query.trim()) { this.error = 'Enter a search question to query indexed documents.'; return; }
    this.loading = true; this.error = ''; this.results = []; this.searched = false;
    this.api.searchDocuments(this.query.trim()).pipe(takeUntilDestroyed(this.destroy)).subscribe({
      next: value => { if (Array.isArray(value)) { this.results = value; this.searched = true; } else this.error = 'Document search returned an invalid response.'; this.loading = false; this.cdr.markForCheck(); },
      error: () => { this.error = 'Document search is unavailable. Check the connection and document permissions, then retry.'; this.loading = false; this.cdr.markForCheck(); }
    });
  }
  ingest(): void {
    if (this.ingesting) return;
    if (!this.filePath.trim() || !this.source.trim()) { this.ingestionError = 'File path and source are required for ingestion.'; return; }
    this.ingesting = true; this.ingestionError = ''; this.message = '';
    this.api.ingestDocument(this.filePath.trim(), this.source.trim()).pipe(takeUntilDestroyed(this.destroy)).subscribe({
      next: value => { this.message = value.message || 'Ingestion request completed. Search to verify indexed content.'; this.ingesting = false; this.cdr.markForCheck(); },
      error: () => { this.ingestionError = 'Ingestion could not be confirmed. Check indexed content before retrying; the server may have processed the request.'; this.ingesting = false; this.cdr.markForCheck(); }
    });
  }
}
