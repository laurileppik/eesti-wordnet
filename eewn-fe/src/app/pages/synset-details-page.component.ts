import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WordService } from '../services/word.service';
import { parseSenseLabel, formatWords } from '../utils/word-utils';

//TODO eraldi meetod?
export interface Sense {
  label: string;
  lemma?: string;
  partOfSpeech?: string;
  status?: string;
  comment?: string;
  examples?: string[];
}

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
  hyponymsExpanded = false;
  hypernymsExpanded = false;
  relationSectionsExpanded: Record<string, boolean> = {};

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
    this.hyponymsExpanded = false;
    this.hypernymsExpanded = false;
    this.relationSectionsExpanded = {};
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

  get definitions(): string[] {
    if (!this.synset?.definitions) return [];
    if (Array.isArray(this.synset.definitions)) return this.synset.definitions;
    if (typeof this.synset.definitions === 'object' && this.synset.definitions !== null) {
      return Object.values(this.synset.definitions).filter(v => typeof v === 'string');
    }
    if (typeof this.synset.definitions === 'string') return [this.synset.definitions];
    return [];
  }

  get labeledSenseExamples(): { lemma: string, subscript: string, example: string }[] {
    if (!this.synset?.senses) return [];
    const result: { lemma: string, subscript: string, example: string }[] = [];
    for (const sense of this.synset.senses as Sense[]) {
      const parsed = parseSenseLabel(sense.label);
      if (Array.isArray(sense.examples) && sense.examples.length) {
        for (const ex of sense.examples) {
          result.push({
            lemma: parsed.lemma,
            subscript: parsed.subscript,
            example: ex
          });
        }
      }
    }
    return result;
  }

  get formattedSenses(): { lemma: string, subscript: string, partOfSpeech?: string, status?: string, comment?: string }[] {
    if (!this.synset?.senses) return [];
    return (this.synset.senses as Sense[]).map(sense => {
      const parsed = parseSenseLabel(sense.label);
      return {
        lemma: sense.lemma ?? '',
        subscript: parsed.subscript,
        partOfSpeech: sense.partOfSpeech,
        status: sense.status,
        comment: sense.comment
      };
    });
  }

  get formattedHeaderSenses(): { lemma: string, subscript: string }[] {
    if (!this.synset?.senses) return [];
    return (this.synset.senses as Sense[]).map(sense => parseSenseLabel(sense.label));
  }

  toggleRelations(): void {
    this.relationsExpanded = !this.relationsExpanded;
  }

  toggleExternalRefs(): void {
    this.externalReferencesExpanded = !this.externalReferencesExpanded;
  }

  toggleHyponyms(): void {
    this.hyponymsExpanded = !this.hyponymsExpanded;
  }

  toggleHypernyms(): void {
    this.hypernymsExpanded = !this.hypernymsExpanded;
  }

  toggleSection(section: string): void {
    this.relationSectionsExpanded[section] = !this.relationSectionsExpanded[section];
  }

  handleToggleKey(event: KeyboardEvent, section: string): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.toggleSection(section);
    }
  }

  goToSynset(synsetId: number) {
    this.router.navigate(['/synsets', synsetId]);
  }

  get hyponyms() {
    return this.synset?.relations?.filter((r: any) => r.type === 'has_hyponym') || [];
  }

  get hypernyms() {
    return this.synset?.relations?.filter((r: any) => r.type === 'has_hypernym') || [];
  }

  get groupedRelations() {
    const rels = this.synset?.relations || [];
    const map: Record<string, any[]> = {};
    for (const r of rels) {
      const t = r.type || 'unspecified';
      map[t] = map[t] || [];
      map[t].push(r);
    }
    const sections: { type: string; label: string; items: any[] }[] = [];
    if (map['has_hyponym']) {
      sections.push({ type: 'has_hyponym', label: this.relationLabel('has_hyponym'), items: map['has_hyponym'] });
    }
    if (map['has_hypernym']) {
      sections.push({ type: 'has_hypernym', label: this.relationLabel('has_hypernym'), items: map['has_hypernym'] });
    }
    const otherTypes = Object.keys(map).filter(t => t !== 'has_hyponym' && t !== 'has_hypernym').sort((a, b) => a.localeCompare(b));
    for (const t of otherTypes) {
      sections.push({ type: t, label: this.relationLabel(t), items: map[t] });
    }
    return sections;
  }

  get otherRelationSections() {
    const rels = this.synset?.relations || [];
    const map: Record<string, any[]> = {};
    for (const r of rels) {
      const t = r.type || 'unspecified';
      if (t === 'has_hyponym' || t === 'has_hypernym') continue;
      map[t] = map[t] || [];
      map[t].push(r);
    }
    return Object.keys(map).sort((a, b) => a.localeCompare(b)).map(type => ({ type, label: this.relationLabel(type), items: map[type] }));
  }

  relationLabel(type: string) {
    if (!type) return 'Seosed';
    // TODO 1 need hardcoded. 2 Siia peaks juurde lisama sense'i, et nt element(1(n)) keemiline element((1n))
    if (type === 'has_hyponym') return 'Hüponüümid';
    if (type === 'has_hypernym') return 'Hüperonüümid';
    return type.replaceAll('_', ' ');
  }

  displayRelevantWords(rel: any): string {
    if (!rel) return '';
    const words: any[] = Array.isArray(rel.relevantWords) ? rel.relevantWords : [];
    const mainLabel = rel.relatedLabel ?? String(rel.relatedSynsetId ?? '');
    const mainId = String(rel.relatedSynsetId ?? '');
    const filtered = words
      .map(String)
      .filter(w => w && w !== String(mainLabel) && w !== mainId);
    //eriti see...
    return filtered.length
      ? filtered.map(word => {
          const parsed = parseSenseLabel(word);
          return parsed.subscript
            ? `${parsed.lemma}<sub class='sense-subscript'>${parsed.subscript}</sub>`
            : parsed.lemma;
        }).join(', ')
      : '';
  }

  getFormattedWords(words: (string | undefined)[]): { lemma: string, subscript: string }[] {
    return formatWords(words);
  }
}
