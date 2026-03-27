import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'menu', pathMatch: 'full' },

  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },

  {
    path: '',
    canActivateChild: [authGuard],
    children: [
      {
        path: 'menu',
        loadComponent: () => import('./pages/menu/menu.component').then(m => m.MenuComponent)
      },

      // ── ADMIN ──────────────────────────────────────────────────────────────
      {
        path: 'admin',
        canActivateChild: [roleGuard],
        data: { roles: ['ADMIN'] },
        children: [
          { path: '', loadComponent: () => import('./pages/admin/admin-main/admin-main.component').then(m => m.AdminMainComponent) },
          { path: 'employees', loadComponent: () => import('./pages/admin/employees/employees.component').then(m => m.EmployeesComponent) },
          { path: 'inventory', loadComponent: () => import('./pages/admin/inventory/inventory.component').then(m => m.InventoryComponent) },
          { path: 'suppliers', loadComponent: () => import('./pages/admin/suppliers/suppliers.component').then(m => m.SuppliersComponent) },
          { path: 'supply-requests', loadComponent: () => import('./pages/admin/supply-requests/supply-requests.component').then(m => m.AdminSupplyRequestsComponent) },
          { path: 'supply-dashboard', loadComponent: () => import('./pages/admin/supply-dashboard/supply-dashboard.component').then(m => m.SupplyDashboardComponent) },
          { path: 'supply-settings', loadComponent: () => import('./pages/admin/supply-settings/supply-settings.component').then(m => m.SupplySettingsComponent) },
          { path: 'notifications', loadComponent: () => import('./pages/admin/notifications/notifications.component').then(m => m.NotificationsComponent) },
          { path: 'audit', loadComponent: () => import('./pages/admin/audit-logs/audit-logs.component').then(m => m.AuditLogsComponent) }
        ]
      },

      // ── RECEPTIONIST ───────────────────────────────────────────────────────
      {
        path: 'receptionist',
        canActivateChild: [roleGuard],
        data: { roles: ['RECEPTIONIST'] },
        children: [
          { path: '', loadComponent: () => import('./pages/receptionist/receptionist-main/receptionist-main.component').then(m => m.ReceptionistMainComponent) },
          { path: 'clients', loadComponent: () => import('./pages/receptionist/clients/clients.component').then(m => m.ClientsComponent) },
          { path: 'create-order', loadComponent: () => import('./pages/receptionist/create-order/create-order.component').then(m => m.CreateOrderComponent) },
          { path: 'orders', loadComponent: () => import('./pages/receptionist/search-orders/search-orders.component').then(m => m.SearchOrdersComponent) },
          { path: 'payments', loadComponent: () => import('./pages/receptionist/payments/payments.component').then(m => m.PaymentsComponent) }
        ]
      },

      // ── TECHNICIAN ─────────────────────────────────────────────────────────
      {
        path: 'technician',
        canActivateChild: [roleGuard],
        data: { roles: ['TECHNICIAN'] },
        children: [
          { path: '', loadComponent: () => import('./pages/technician/technician-main/technician-main.component').then(m => m.TechnicianMainComponent) },
          { path: 'unassigned', loadComponent: () => import('./pages/technician/unassigned-orders/unassigned-orders.component').then(m => m.UnassignedOrdersComponent) },
          { path: 'my-orders', loadComponent: () => import('./pages/technician/my-orders/my-orders.component').then(m => m.MyOrdersComponent) },
          { path: 'orders/:id', loadComponent: () => import('./pages/technician/order-detail/order-detail.component').then(m => m.OrderDetailComponent) },
          { path: 'warehouse', loadComponent: () => import('./pages/technician/warehouse/warehouse.component').then(m => m.WarehouseComponent) },
          { path: 'supply-requests', loadComponent: () => import('./pages/technician/supply-requests/supply-requests.component').then(m => m.TechSupplyRequestsComponent) }
        ]
      }
    ]
  },

  { path: '**', redirectTo: 'menu' }
];
