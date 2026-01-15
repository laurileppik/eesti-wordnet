import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WordService, WordWithDefinitionDto } from '../../services/word.service';

@Component({
  selector: 'app-word-tree-node',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './word-tree-node.component.html',
})
export class WordTreeNodeComponent {
  @Input() word!: WordWithDefinitionDto;
  @Input() relationType: string | null = null;

  expanded = false;
  loading = false;
  relations: { [type: string]: WordWithDefinitionDto[] } = {};

  constructor(private wordService: WordService) {}

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
