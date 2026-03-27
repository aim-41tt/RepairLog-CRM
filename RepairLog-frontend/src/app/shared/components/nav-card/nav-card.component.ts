import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-nav-card',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './nav-card.component.html',
  styleUrl: './nav-card.component.scss'
})
export class NavCardComponent {
  @Input({ required: true }) title!: string;
  @Input() description = '';
  @Input({ required: true }) icon!: string;
  @Input({ required: true }) route!: string;
}
