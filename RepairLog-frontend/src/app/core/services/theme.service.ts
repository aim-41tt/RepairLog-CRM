import { Injectable, signal } from '@angular/core';

export type ThemeMode = 'light' | 'dark' | 'system';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'app-theme';
  readonly mode = signal<ThemeMode>('system');

  constructor() {
    const saved = localStorage.getItem(this.STORAGE_KEY) as ThemeMode | null;
    this.applyTheme(saved ?? 'system');

    window.matchMedia('(prefers-color-scheme: dark)')
      .addEventListener('change', () => {
        if (this.mode() === 'system') this.applyTheme('system');
      });
  }

  setTheme(mode: ThemeMode): void {
    localStorage.setItem(this.STORAGE_KEY, mode);
    this.applyTheme(mode);
  }

  private applyTheme(mode: ThemeMode): void {
    this.mode.set(mode);
    const resolved = mode === 'system'
      ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
      : mode;
    document.documentElement.classList.remove('theme-light', 'theme-dark');
    document.documentElement.classList.add(`theme-${resolved}`);
  }
}
