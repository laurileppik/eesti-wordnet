import { Routes } from '@angular/router';

//TODO kõik muu routei tagasi / peale (nt suvaline URL)
export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/search-page/search-page.component').then(m => m.SearchPageComponent),
  },
  {
    path: 'synsets/:id',
    loadComponent: () =>
      import('./pages/synset-details-page.component').then(m => m.SynsetDetailsPageComponent),
  },
];
