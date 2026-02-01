import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { keycloak } from '../auth/keycloak';

export const roleGuard = (expectedRoles: string[]): CanActivateFn => {
  return () => {
    const router = inject(Router);

    if (!keycloak.authenticated) {
      router.navigate(['/landing']);
      return false;
    }

    const token = keycloak.tokenParsed as any;

    // ✅ Realm roles (recommended)
    const userRoles: string[] =
      token?.realm_access?.roles ?? [];

    const hasRole = expectedRoles.some(role =>
      userRoles.map(r => r.toLowerCase()).includes(role.toLowerCase())
    );

    if (!hasRole) {
      router.navigate(['/landing']);
      return false;
    }

    return true;
  };
};
