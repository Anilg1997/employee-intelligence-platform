import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  RagService,
  RagResponse
} from '../../services/rag.service';

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

  constructor(private ragService: RagService) {}

  askQuestion(): void {

    this.errorMessage = '';
    this.response = null;

    const question = this.question.trim();

    if (!question) {
      this.errorMessage = 'Please enter an HR question.';
      return;
    }

    this.loading = true;

    this.ragService.askQuestion(question).subscribe({

      next: (result: RagResponse) => {
        this.response = result;
        this.loading = false;
      },

      error: (error: unknown) => {
        console.error('HR Assistant error:', error);

        this.errorMessage =
          'Unable to get an answer from the HR Assistant.';

        this.loading = false;
      }
    });
  }
}