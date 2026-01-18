import { Routes } from '@angular/router';
import { SearchPageComponent } from './pages/search-page/search-page.component';
import { SynsetDetailsPageComponent } from './pages/synset-details-page.component';

export const routes: Routes = [
  { path: '', component: SearchPageComponent },
  { path: 'synsets/:id', component: SynsetDetailsPageComponent },
];
