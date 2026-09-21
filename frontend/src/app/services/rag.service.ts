import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

  private readonly apiUrl =
    'http://localhost:8080/api/rag/ask';

  constructor(private http: HttpClient) {}

  askQuestion(question: string): Observable<RagResponse> {

    return this.http.post<RagResponse>(
      this.apiUrl,
      { question }
    );
  }
}