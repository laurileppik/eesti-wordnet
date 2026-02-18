import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { WordService, WordWithDefinitionDto } from '../../services/word.service';

@Component({
  selector: 'app-word-tree-node',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
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
  externalReferences: any = null;

  constructor(private readonly wordService: WordService) {}

  ngOnInit() {
    if (this.word?.relevantWords) {
      this.relevantWords = this.word.relevantWords.filter(w => w !== this.word.lemma);
    }
    if (this.word?.externalReferences && Array.isArray(this.word.externalReferences)) {
      this.externalReferences = this.word.externalReferences.find((ref: any) => ref.definition && ref.definition.trim() !== '');
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

  get externalReferenceWords(): string {
    if (this.externalReferences?.words) {
      return this.externalReferences.words
                 .map((w: string) => w.trim())
                 .join(', ');
    }
    return '';
  }
}
