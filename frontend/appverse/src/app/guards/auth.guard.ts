import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { keycloak } from '../auth/keycloak';

export const authGuard: CanActivateFn = async () => {
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  // Already logged in
  if (keycloak.authenticated) {
    return true;
  }

  // Optional UX feedback
  snackBar.open('Please log in to continue', 'Close', {
    duration: 3000,
  });

  // Redirect to Keycloak login
  await keycloak.login({
    redirectUri: window.location.href,
  });

  return false;
};
