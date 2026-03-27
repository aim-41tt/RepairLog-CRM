import { Injectable, computed, signal } from '@angular/core';
import { UserProfile } from '../models/user.models';
import { RoleName } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly STORAGE_KEY = 'userProfile';

  readonly profile = signal<UserProfile | null>(null);
  readonly fullName = computed(() => this.profile()?.fullName ?? '');
  readonly roles = computed(() => this.profile()?.roles ?? []);

  constructor() {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    if (stored) {
      try { this.profile.set(JSON.parse(stored)); } catch { /* ignore */ }
    }
  }

  setProfile(profile: UserProfile): void {
    this.profile.set(profile);
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(profile));
  }

  clearProfile(): void {
    this.profile.set(null);
    localStorage.removeItem(this.STORAGE_KEY);
  }

  hasRole(role: RoleName): boolean {
    return this.roles().includes(role);
  }
}
