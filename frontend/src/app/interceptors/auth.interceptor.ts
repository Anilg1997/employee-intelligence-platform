import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  return (token ? next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })) : next(request)).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 || error.status === 403) auth.recordAuthError(error.status);
      return throwError(() => error);
    })
  );
};
