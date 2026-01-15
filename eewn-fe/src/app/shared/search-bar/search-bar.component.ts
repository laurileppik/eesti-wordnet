import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { WordService, WordWithDefinitionDto } from '../../services/word.service';
import { WordTreeNodeComponent } from '../word-tree-node/word-tree-node.component';

@Component({
  selector: 'app-search-bar',
  templateUrl: './search-bar.component.html',
  imports: [
    FormsModule,
    CommonModule,
    WordTreeNodeComponent,
  ],
})
export class SearchBarComponent {
  query = '';
  results: WordWithDefinitionDto[] = [];

  constructor(private readonly wordService: WordService) {}

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
}
