import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WordService } from '../services/word.service';

@Component({
  selector: 'app-synset-details-page',
  templateUrl: './synset-details-page.component.html',
  styleUrls: ['./synset-details-page.component.scss'],
  standalone: true,
  imports: [
    RouterLink,
    CommonModule,
  ],
})
export class SynsetDetailsPageComponent implements OnInit {
  synsetId!: number;
  synset: any;
  loading = true;
  error = false;

  constructor(private route: ActivatedRoute, private wordService: WordService) {}

  ngOnInit(): void {
    this.synsetId = Number(this.route.snapshot.paramMap.get('id'));
    this.wordService.getSynsetDetails(this.synsetId).subscribe({
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
    if (!this.synset || !this.synset.definitions) return [];
    if (Array.isArray(this.synset.definitions)) return this.synset.definitions;
    if (typeof this.synset.definitions === 'object' && this.synset.definitions !== null) {
      return Object.values(this.synset.definitions).filter(v => typeof v === 'string');
    }
    if (typeof this.synset.definitions === 'string') return [this.synset.definitions];
    return [];
  }
}
