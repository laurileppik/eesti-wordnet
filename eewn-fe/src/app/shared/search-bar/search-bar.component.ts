import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { WordService, WordWithDefinitionDto } from '../../services/word.service';

@Component({
  selector: 'app-search-bar',
  templateUrl: './search-bar.component.html',
  imports: [
    FormsModule,
    CommonModule,
  ],
})
export class SearchBarComponent {
  query = '';
  results: WordWithDefinitionDto[] = [];
  selectedWord: WordWithDefinitionDto | null = null;
  wordDetails: any = null;

  constructor(private readonly wordService: WordService) {}

  onSearch() {
    if (!this.query.trim()) return;
    this.wordService.searchWords(this.query)
      .subscribe((data) => {
        this.results = data.filter(word => word.lemma.toLowerCase() === this.query.trim().toLowerCase());
        this.selectedWord = null;
        this.wordDetails = null;
      });
  }

  selectWord(word: WordWithDefinitionDto) {
    this.selectedWord = word;
    this.wordService.getWordDetails(word.id)
      .subscribe(details => {
        this.wordDetails = details;
      });
  }
}
