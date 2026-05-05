import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { getKeycloak } from '../auth/keycloak';

export const roleGuard = (expectedRoles: string[]): CanActivateFn => {
  return () => {
    const router = inject(Router);

    try {
      const kc = getKeycloak();

      if (!kc.authenticated) {
        router.navigate(['/landing']);
        return false;
      }

      const token = kc.tokenParsed as any;

      const userRoles: string[] =
        token?.realm_access?.roles ?? [];

      const hasRole = expectedRoles.some(role =>
        userRoles.some(r => r.toLowerCase() === role.toLowerCase())
      );

      if (!hasRole) {
        router.navigate(['/landing']); // or /unauthorized
        return false;
      }

      return true;
    } catch {
      router.navigate(['/landing']);
      return false;
    }
  };
};