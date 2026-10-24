import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  RagService,
  RagResponse
} from '../../services/rag.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-hr-assistant',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './hr-assistant.html',
  styleUrl: './hr-assistant.scss'
})
export class HrAssistant {

  question = '';
  response: RagResponse | null = null;

  loading = false;
  errorMessage = '';

  constructor(
    private readonly ragService: RagService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  askQuestion(): void {
    if (this.loading) {
      return;
    }

    this.errorMessage = '';
    this.response = null;

    const question = this.question.trim();

    if (!question) {
      this.errorMessage = 'Please enter an HR question.';
      return;
    }

    this.loading = true;

    this.ragService.askQuestion(question)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({

      next: (result: RagResponse) => {
        this.response = result;
      },

      error: () => {
        this.errorMessage = 'Unable to get an answer from the HR Assistant. Check policy documents, database, and Ollama availability, then retry.';
      }
    });
  }
}