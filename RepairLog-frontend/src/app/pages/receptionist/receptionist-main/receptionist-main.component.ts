import { Component } from '@angular/core';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { NavCardComponent } from '../../../shared/components/nav-card/nav-card.component';

@Component({
  selector: 'app-receptionist-main',
  standalone: true,
  imports: [PageLayoutComponent, NavCardComponent],
  templateUrl: './receptionist-main.component.html',
  styleUrl: './receptionist-main.component.scss'
})
export class ReceptionistMainComponent {}
