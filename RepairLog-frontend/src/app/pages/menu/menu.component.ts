import { Component, inject, computed } from '@angular/core';
import { UserService } from '../../core/services/user.service';
import { RoleName } from '../../core/models/auth.models';
import { PageLayoutComponent } from '../../shared/components/page-layout/page-layout.component';
import { NavCardComponent } from '../../shared/components/nav-card/nav-card.component';

interface RoleCard {
  role: RoleName;
  title: string;
  description: string;
  icon: string;
  route: string;
}

const ROLE_CARDS: RoleCard[] = [
  { role: 'ADMIN',        title: 'Администратор', description: 'Управление системой',    icon: 'icons/shield.svg',    route: '/admin' },
  { role: 'TECHNICIAN',   title: 'Мастер',         description: 'Рабочее место мастера',  icon: 'icons/wrench.svg',    route: '/technician' },
  { role: 'RECEPTIONIST', title: 'Приёмщик',       description: 'Рабочее место приёмщика', icon: 'icons/clipboard.svg', route: '/receptionist' }
];

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [PageLayoutComponent, NavCardComponent],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent {
  private userService = inject(UserService);

  cards = computed(() =>
    ROLE_CARDS.filter(c => this.userService.hasRole(c.role))
  );
}
