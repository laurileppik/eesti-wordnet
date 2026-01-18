import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WordService, WordWithDefinitionDto } from '../../services/word.service';

@Component({
  selector: 'app-word-tree-node',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './word-tree-node.component.html',
  styleUrl: './word-tree-node.component.scss',
})
export class WordTreeNodeComponent implements OnInit {
  @Input() word!: WordWithDefinitionDto;
  @Input() relationType: string | null = null;

  expanded = false;
  loading = false;
  relations: { [type: string]: WordWithDefinitionDto[] } = {};
  relevantWords: string[] = [];

  constructor(private readonly wordService: WordService) {}

  ngOnInit() {
    if (this.word?.relevantWords) {
      this.relevantWords = this.word.relevantWords.filter(w => w !== this.word.lemma);
    }
  }

  toggleExpand() {
    this.expanded = !this.expanded;
    if (this.expanded && Object.keys(this.relations).length === 0 && this.word.synsetId) {
      this.loading = true;
      this.wordService.getSynsetRelations(this.word.synsetId).subscribe(relations => {
        this.relations = relations;
        this.loading = false;
      });
    }
  }

  objectKeys = Object.keys;
}
