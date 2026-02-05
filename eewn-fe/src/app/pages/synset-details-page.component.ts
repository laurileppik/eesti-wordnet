import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WordService } from '../services/word.service';

@Component({
  selector: 'app-synset-details-page',
  templateUrl: './synset-details-page.component.html',
  styleUrls: ['./synset-details-page.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
  ],
})
export class SynsetDetailsPageComponent implements OnInit {
  synsetId!: number;
  synset: any;
  loading = true;
  error = false;
  relationsExpanded = false;
  externalReferencesExpanded = false;

  constructor(private readonly route: ActivatedRoute, private readonly wordService: WordService, private readonly router: Router) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      this.loadSynset(id);
    });
  }

  loadSynset(id: number) {
    this.synsetId = id;
    this.loading = true;
    this.error = false;
    this.relationsExpanded = false;
    this.externalReferencesExpanded = false;
    this.wordService.getSynsetDetails(id).subscribe({
      next: (data) => {
        if (!Array.isArray(data.definitions)) {
          if (typeof data.definitions === 'object' && data.definitions !== null) {
            data.definitions = Object.values(data.definitions).filter(v => typeof v === 'string');
          } else if (typeof data.definitions === 'string') {
            data.definitions = [data.definitions];
          } else {
            data.definitions = [];
          }
        }
        this.synset = data;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  get definitionsArray(): string[] {
    if (!this.synset?.definitions) return [];
    if (Array.isArray(this.synset.definitions)) return this.synset.definitions;
    if (typeof this.synset.definitions === 'object' && this.synset.definitions !== null) {
      return Object.values(this.synset.definitions).filter(v => typeof v === 'string');
    }
    if (typeof this.synset.definitions === 'string') return [this.synset.definitions];
    return [];
  }

  getSenseSubscript(sense: any): string {
    let senseNumber = '';
    if (sense.label) {
      const match = sense.label.match(/_(\d+)\(/);
      if (match) {
        senseNumber = ` ${match[1]}`;
      }
    }
    const pos = sense.partOfSpeech ? `(${sense.partOfSpeech})` : '';
    return `${senseNumber}${pos}`;
  }

  toggleRelations(): void {
    this.relationsExpanded = !this.relationsExpanded;
  }

  toggleExternalReferences(): void {
    this.externalReferencesExpanded = !this.externalReferencesExpanded;
  }

  onToggleKeyDown(event: KeyboardEvent, section: 'relations' | 'externalReferences'): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      if (section === 'relations') {
        this.toggleRelations();
      } else if (section === 'externalReferences') {
        this.toggleExternalReferences();
      }
    }
  }

  onRelationClick(synsetId: number) {
    this.router.navigate(['/synsets', synsetId]);
  }
}
