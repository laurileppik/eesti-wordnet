import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

//TODO DTO-de jaoks eraldi failid ja kaust
export interface ExternalReferenceDto {
  systemName: string;
  relationType: string;
  reference: string;
  words: string[];
  definition: string;
}

export interface WordWithDefinitionDto {
  id: number;
  lemma: string;
  partOfSpeech: string;
  definition?: string;
  label?: string;
  synsetId?: number;
  relevantWords?: string[];
  externalReferences?: ExternalReferenceDto[];
}

export interface AutocompleteWordDto {
  id: number;
  lemma: string;
}

@Injectable({ providedIn: 'root' })
export class WordService {
  private readonly http = inject(HttpClient);

  constructor() {}

  searchWords(query: string): Observable<WordWithDefinitionDto[]> {
    return this.http.get<WordWithDefinitionDto[]>(`${environment.apiUrl}/api/search?query=${encodeURIComponent(query)}`);
  }

  getSynsetRelations(synsetId: number): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/api/synset/${synsetId}/relations`);
  }
  //TODO siin veel kasutamata?

  getWordBySynsetId(synsetId: number): Observable<WordWithDefinitionDto> {
    return this.http.get<WordWithDefinitionDto>(`${environment.apiUrl}/api/synset/${synsetId}/word`);
  }

  getRelevantWords(wordId: number): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiUrl}/api/word/${wordId}/relevant-words`);
  }

  autocompleteWords(query: string): Observable<AutocompleteWordDto[]> {
    return this.http.get<AutocompleteWordDto[]>(`${environment.apiUrl}/api/autocomplete?query=${encodeURIComponent(query)}`);
  }

  getSynsetDetails(id: number): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/api/synsets/${id}`);
  }
}
