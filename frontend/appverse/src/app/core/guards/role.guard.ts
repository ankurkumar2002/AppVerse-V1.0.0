import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { getKeycloak } from '../auth/keycloak';

export const roleGuard = (expectedRoles: string[]): CanActivateFn => {

  return async () => {

    const router = inject(Router);

    try {

      const kc = getKeycloak();

      if (!kc.authenticated) {
        return router.createUrlTree(['/landing']);
      }

      await kc.updateToken(30);

      const token = kc.tokenParsed as any;

      const userRoles: string[] =
        token?.realm_access?.roles ?? [];

      console.log('USER ROLES:', userRoles);
      console.log('EXPECTED ROLES:', expectedRoles);

      const hasRole = expectedRoles.some(expectedRole =>
        userRoles.some(userRole =>
          userRole.toLowerCase() === expectedRole.toLowerCase()
        )
      );

      if (!hasRole) {

        console.warn(
          'Unauthorized access attempt.',
          {
            userRoles,
            expectedRoles
          }
        );

        sessionStorage.setItem(
          'authError',
          'You do not have permission to access this page. Please login with the correct credentials.'
        );

        await kc.logout({
          redirectUri: `${window.location.origin}/landing`
        });

        return false;
      }

      return true;

    } catch (error) {

      console.error(
        'Role authorization check failed:',
        error
      );

      sessionStorage.setItem(
        'authError',
        'Unable to verify your permissions. Please login again with the correct credentials.'
      );

      try {

        const kc = getKeycloak();

        await kc.logout({
          redirectUri: `${window.location.origin}/landing`
        });

      } catch (logoutError) {

        console.error(
          'Keycloak logout failed:',
          logoutError
        );

      }

      return false;
    }
  };
};