import { Injectable } from '@angular/core';
import { WordWithDefinitionDto } from './word.service';

@Injectable({ providedIn: 'root' })
export class SearchStateService {
  query: string = '';
  results: WordWithDefinitionDto[] = [];
  selectedWordId: number | null = null;

  clearSelection() {
    this.selectedWordId = null;
  }
}
