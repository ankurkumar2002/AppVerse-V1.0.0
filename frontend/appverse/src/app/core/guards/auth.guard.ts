import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { getKeycloak } from '../auth/keycloak';

export const authGuard: CanActivateFn = async () => {
  const router = inject(Router);

  try {
    const kc = getKeycloak();

    if (kc.authenticated) {
      return true;
    }

    await kc.login({
      redirectUri: window.location.href
    });

    return false;
  } catch {
    router.navigate(['/landing']);
    return false;
  }
};