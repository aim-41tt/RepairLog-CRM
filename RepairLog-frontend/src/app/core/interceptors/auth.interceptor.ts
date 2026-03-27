import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const toast = inject(ToastService);
  const token = authService.getToken();

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.includes('/auth/login')) {
        authService.logout();
        toast.error('Сессия истекла, войдите снова');
      } else if (err.status === 403) {
        toast.error('Нет доступа');
      } else if (err.status >= 400 && err.status < 500) {
        const message = extractMessage(err);
        toast.error(message);
      } else if (err.status >= 500) {
        toast.error('Ошибка сервера. Попробуйте позже');
      } else if (err.status === 0) {
        toast.error('Нет связи с сервером');
      }
      return throwError(() => err);
    })
  );
};

function extractMessage(err: HttpErrorResponse): string {
  if (typeof err.error === 'string') return err.error;
  if (err.error?.message) return err.error.message;
  if (err.error?.error) return err.error.error;
  return err.message || 'Произошла ошибка';
}
