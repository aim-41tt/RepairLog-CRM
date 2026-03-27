import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { PageLayoutComponent } from '../../../shared/components/page-layout/page-layout.component';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification } from '../../../core/models/notification.models';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [PageLayoutComponent, RouterLink, DatePipe],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.scss'
})
export class NotificationsComponent implements OnInit {
  private notifService = inject(NotificationService);

  notifications = signal<Notification[]>([]);
  loading = signal(false);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.notifService.getPending().subscribe({
      next: n => { this.notifications.set(n); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  markSent(id: number): void {
    this.notifService.markSent(id).subscribe({ next: () => this.load() });
  }
}
