import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { getKeycloak } from '../auth/keycloak';
import { UserAuthService } from '../../features/user/services/user/user-auth.service';

export const profileCompletionGuard: CanActivateFn = async () => {

  const router = inject(Router);
  const userAuthService = inject(UserAuthService);

  console.log('========== PROFILE COMPLETION GUARD ==========');

  const keycloak = getKeycloak();

  // Check authentication
  if (!keycloak.authenticated) {

    console.warn('User is NOT authenticated. Redirecting to landing page.');

    return router.createUrlTree(['/landing']);
  }

  console.log('User is authenticated.');

  try {

    // Load Keycloak profile
    const profile = await keycloak.loadUserProfile();

    console.log('Loaded Keycloak profile:', profile);

    const keycloakId = profile?.id;

    if (!keycloakId) {

      console.warn('Keycloak ID missing. Allowing access to profile completion.');

      return true;
    }

    console.log('Keycloak ID:', keycloakId);

    // Fetch user from backend
    const exists = await firstValueFrom(
      userAuthService.getUserByKeycloakId(keycloakId)
    );

    console.log('Backend user fetched:', exists);

    // Profile already completed
    if (exists) {

      console.log(
        'Profile already completed. Redirecting to /user/dashboard'
      );

      return router.createUrlTree(['/user/dashboard']);
    }

    console.log(
      'Profile NOT completed. Allowing access to profile completion page.'
    );

    return true;

  } catch (error) {

    console.error(
      'Error while checking profile completion:',
      error
    );

    console.log(
      'Assuming profile incomplete. Allowing access to completion page.'
    );

    return true;
  }
};