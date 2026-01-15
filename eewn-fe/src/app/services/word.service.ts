import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface WordWithDefinitionDto {
  id: number;
  lemma: string;
  partOfSpeech: string;
  definition?: string;
  label?: string;
  synsetId?: number;
  relevantWords?: string[];
}

@Injectable({ providedIn: 'root' })
export class WordService {
  private readonly http = inject(HttpClient);

  constructor() {}

  searchWords(query: string): Observable<WordWithDefinitionDto[]> {
    return this.http.get<WordWithDefinitionDto[]>(`${environment.apiUrl}/api/search?query=${encodeURIComponent(query)}`);
  }

  getWordDetails(id: number): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/api/word/${id}`);
  }

  getSynsetRelations(synsetId: number): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/api/synset/${synsetId}/relations`);
  }

  getWordBySynsetId(synsetId: number): Observable<WordWithDefinitionDto> {
    return this.http.get<WordWithDefinitionDto>(`${environment.apiUrl}/api/synset/${synsetId}/word`);
  }

  getRelevantWords(wordId: number): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiUrl}/api/word/${wordId}/relevant-words`);
  }
}
