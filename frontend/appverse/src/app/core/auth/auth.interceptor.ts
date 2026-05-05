import { HttpInterceptorFn } from '@angular/common/http';
import { getKeycloak } from './keycloak';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = getKeycloak().token;

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(req);
};
