import { Component, Input } from '@angular/core';
import { NavbarComponent, NavbarRole } from '../navbar/navbar.component';

@Component({
  selector: 'app-page-layout',
  standalone: true,
  imports: [NavbarComponent],
  templateUrl: './page-layout.component.html',
  styleUrl: './page-layout.component.scss'
})
export class PageLayoutComponent {
  @Input() role: NavbarRole = 'base';
}
