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

  onRelationClick(synsetId: number) {
    this.router.navigate(['/synsets', synsetId]);
  }
}
