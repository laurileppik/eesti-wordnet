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
  relations: { [type: string]: any[] } = {};

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
    this.wordDetails = null;
    this.relations = {};
    console.log('SEL WORD:', word);
    if (word.synsetId) {
      console.log('Fetching relations synID:', word.synsetId);
      this.wordService.getSynsetRelations(word.synsetId)
        .subscribe(relations => {
          console.log('REL:', relations);
          this.relations = relations;
        });
    } else {
      console.warn('NOT FOUND');
    }
  }

  protected readonly Object = Object;
}
