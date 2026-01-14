import { Component } from '@angular/core';
import { SearchBarComponent } from './shared/search-bar/search-bar.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  imports: [
    SearchBarComponent,
  ],
})
export class AppComponent {}
