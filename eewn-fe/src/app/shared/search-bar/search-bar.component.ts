import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatIconModule } from '@angular/material/icon';
import { WordService, WordWithDefinitionDto } from '../../services/word.service';
import { WordTreeNodeComponent } from '../word-tree-node/word-tree-node.component';

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
    WordTreeNodeComponent,
  ],
})
export class SearchBarComponent {
  query = '';
  results: WordWithDefinitionDto[] = [];
  filteredOptions: string[] = [];
  private allOptions: string[] = [];

  constructor(private readonly wordService: WordService) {}

  onInput() {
    if (this.query.length > 1) {
      this.wordService.searchWords(this.query).subscribe(words => {
        this.allOptions = words.map(w => w.lemma);
        this.filteredOptions = this.allOptions.filter(option =>
          option.toLowerCase().includes(this.query.toLowerCase())
        );
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
    if (!this.query.trim()) return;
    this.wordService.searchWords(this.query)
      .subscribe((data) => {
        this.results = data.filter(word => word.lemma.toLowerCase() === this.query.trim().toLowerCase());
      });
  }

  get groupedResults() {
    const groups: { [key: string]: { title: string, words: WordWithDefinitionDto[] } } = {};
    const posMap: { [key: string]: string } = {
      n: 'Nimisõna',
      v: 'Tegusõna',
      b: 'Määrsõna',
      a: 'Omadussõna',
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
}
