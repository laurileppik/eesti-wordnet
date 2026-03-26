import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { WordService, WordWithDefinitionDto } from '../../services/word.service';
import { formatWords } from '../../utils/word-utils';
import { SearchStateService } from '../../services/search-state.service';

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

  constructor(private readonly wordService: WordService, public readonly searchState: SearchStateService) {}

  ngOnInit() {
    if (this.word?.relevantWords) {
      this.relevantWords = this.word.relevantWords;
    }
    if (this.word?.externalReferences && Array.isArray(this.word.externalReferences)) {
      const candidates = this.word.externalReferences.filter((ref: any) => ref?.definition && String(ref.definition).trim() !== '');
      if (candidates.length > 0) {
        const findByName = (name: string) => candidates.find((r: any) => String(r.systemName ?? '').toUpperCase().includes(name));
        this.externalReferences = findByName('PWN-3.0') || findByName('CILI') || candidates[0];
      } else {
        this.externalReferences = null;
      }
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

  get formattedRelevantWords(): { lemma: string, subscript: string }[] {
    return formatWords(this.relevantWords);
  }

  get formattedExternalReferenceWords(): { lemma: string, subscript: string }[] {
    if (this.externalReferences?.words) {
      return formatWords(this.externalReferences.words);
    }
    return [];
  }
}
