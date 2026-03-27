import { Component } from '@angular/core';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { NavCardComponent } from '../../../shared/components/nav-card/nav-card.component';

@Component({
  selector: 'app-technician-main',
  standalone: true,
  imports: [PageLayoutComponent, NavCardComponent],
  templateUrl: './technician-main.component.html',
  styleUrl: './technician-main.component.scss'
})
export class TechnicianMainComponent {}
