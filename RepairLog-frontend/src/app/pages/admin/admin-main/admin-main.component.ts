import { Component } from '@angular/core';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { NavCardComponent } from '../../../shared/components/nav-card/nav-card.component';

@Component({
  selector: 'app-admin-main',
  standalone: true,
  imports: [PageLayoutComponent, NavCardComponent],
  templateUrl: './admin-main.component.html',
  styleUrl: './admin-main.component.scss'
})
export class AdminMainComponent {}
