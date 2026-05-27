import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { getKeycloak } from '../auth/keycloak';

export const roleGuard = (expectedRoles: string[]): CanActivateFn => {

  return async () => {

    const router = inject(Router);

    try {

      const kc = getKeycloak();

      // FORCE TOKEN REFRESH
      await kc.updateToken(0);

      if (!kc.authenticated) {
        return router.createUrlTree(['/landing']);
      }

      const token = kc.tokenParsed as any;

      const userRoles: string[] =
        token?.realm_access?.roles ?? [];

      console.log('UPDATED USER ROLES:', userRoles);

      const hasRole = expectedRoles.some(role =>
        userRoles.some(r => r.toLowerCase() === role.toLowerCase())
      );

      if (!hasRole) {
        return router.createUrlTree(['/landing']);
      }

      return true;

    } catch (error) {

      console.error(error);

      return router.createUrlTree(['/landing']);
    }
  };
};