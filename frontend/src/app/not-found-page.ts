import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found-page',
  imports: [RouterLink],
  template: `
    <h1>Page not found</h1>
    <p>There is nothing at this address. <a routerLink="/rooms">Go to rooms</a>.</p>
  `,
})
export class NotFoundPage {}
