import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { keycloak } from '../auth/keycloak';
import { UserAuthService } from '../core/services/user/user-auth.service';

export const profileCompletionGuard: CanActivateFn = async () => {
  const router = inject(Router);
  const userAuthService = inject(UserAuthService);

  // At this point, authGuard has already run
  if (!keycloak.authenticated) {
    router.navigate(['/landing']);
    return false;
  }

  // Load profile from Keycloak
  const profile = await keycloak.loadUserProfile();
  const keycloakId = profile?.id;

  if (!keycloakId) {
    router.navigate(['/user/profile-completion']);
    return false;
  }

  try {
    const user = await firstValueFrom(
      userAuthService.getUserByKeycloakId(keycloakId)
    );

    if (user && user.username) {
      return true; // ✅ profile complete
    }

    router.navigate(['/user/profile-completion']);
    return false;
  } catch {
    router.navigate(['/user/profile-completion']);
    return false;
  }
};
