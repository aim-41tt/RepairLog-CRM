import { Component, HostBinding, Input, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ThemeService, ThemeMode } from '../../../core/services/theme.service';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';

export type NavbarRole = 'admin' | 'technician' | 'receptionist' | 'base';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  @Input() role: NavbarRole = 'base';

  @HostBinding('class') get hostClass() {
    return `navbar-${this.role}`;
  }

  themeService = inject(ThemeService);
  userService = inject(UserService);
  private authService = inject(AuthService);

  onThemeChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as ThemeMode;
    this.themeService.setTheme(value);
  }

  logout(): void {
    this.authService.logout();
  }
}
