import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api.config';

export interface RagSource {
  source: string;
  similarity: number;
  content: string;
}

export interface RagResponse {
  question: string;
  answer: string;
  sources: RagSource[];
}

@Injectable({
  providedIn: 'root'
})
export class RagService {

  private readonly ragUrl = apiUrl('rag/ask');

  constructor(private http: HttpClient) {}

  askQuestion(question: string): Observable<RagResponse> {

    return this.http.post<RagResponse>(
      this.ragUrl,
      { question }
    );
  }
}
