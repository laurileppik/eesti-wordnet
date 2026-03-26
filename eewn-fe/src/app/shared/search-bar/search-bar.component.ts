import { Component, ViewChild, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatAutocompleteModule, MatAutocomplete, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { WordService, WordWithDefinitionDto, AutocompleteWordDto } from '../../services/word.service';
import { SearchStateService } from '../../services/search-state.service';
import { WordTreeNodeComponent } from '../word-tree-node/word-tree-node.component';
import { of } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.scss'],
  imports: [
    FormsModule,
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatAutocompleteModule,
    MatIconModule,
    MatSnackBarModule,
    WordTreeNodeComponent,
  ],
})
export class SearchBarComponent implements OnInit {
  query = '';
  results: WordWithDefinitionDto[] = [];
  filteredOptions: AutocompleteWordDto[] = [];
  private autocompleteDebounce$: any;
  @ViewChild('auto') autocompletePanel?: MatAutocomplete;
  @ViewChild(MatAutocompleteTrigger) autocompleteTrigger?: MatAutocompleteTrigger;

  constructor(private readonly wordService: WordService, private readonly snackBar: MatSnackBar, public readonly searchState: SearchStateService) {}

  ngOnInit() {
    if (this.searchState.query) {
      this.query = this.searchState.query;
      this.results = this.searchState.results || [];
      if (this.searchState.selectedWordId) {
        setTimeout(() => this.scrollToSelected(), 150);
      }
    }
  }

  onInput() {
    if (this.query.length > 0) {
      if (this.autocompleteDebounce$) {
        this.autocompleteDebounce$.unsubscribe();
      }
      this.autocompleteDebounce$ = of(this.query).pipe(
        debounceTime(200),
        switchMap(q => this.wordService.autocompleteWords(q))
      ).subscribe(options => {
        this.filteredOptions = options.slice(0, 10);
        if (this.filteredOptions.length === 0) {
          this.snackBar.open('Tulemusi ei leitud', 'OK', { duration: 2000, panelClass: 'mat-warn' });
        }
      });
    } else {
      this.filteredOptions = [];
    }
  }

  onAutocompleteSelect(event: any) {
    this.query = event.option.value;
    this.onSearch();
  }

  onSearch() {
    if (this.query.length > 0) {
      this.wordService.searchWords(this.query).subscribe(words => {
        this.results = words;
        this.autocompleteTrigger?.closePanel();
        if (words.length === 0) {
          this.snackBar.open('Tulemusi ei leitud', 'OK', { duration: 4000, panelClass: 'mat-warn' });
        }
        this.searchState.query = this.query;
        this.searchState.results = this.results;
      });
    }
  }

  get groupedResults() {
    const groups: { [key: string]: { title: string, words: WordWithDefinitionDto[] } } = {};
    const posMap: { [key: string]: string } = {
      n: 'Nimisõnad',
      v: 'Tegusõnad',
      b: 'Määrsõnad',
      a: 'Omadussõnad',
    };
    for (const word of this.results) {
      const pos = word.partOfSpeech;
      if (!groups[pos]) {
        groups[pos] = { title: posMap[pos] || pos, words: [] };
      }
      groups[pos].words.push(word);
    }
    return Object.values(groups);
  }

  trackGroup(index: number, group: any) {
    return group.title;
  }

  trackWord(index: number, word: WordWithDefinitionDto) {
    return word.id;
  }

  private scrollToSelected() {
    const id = this.searchState.selectedWordId;
    if (!id) return;
    const el = document.querySelector(`[data-word-id="${id}"]`);
    if (el) {
      (el as HTMLElement).scrollIntoView({ behavior: 'smooth', block: 'start' });
      el.classList.add('search-selected-highlight');
      setTimeout(() => el.classList.remove('search-selected-highlight'), 1500);
    }
    this.searchState.clearSelection();
  }
}
