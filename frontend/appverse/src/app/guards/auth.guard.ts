import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private keycloak: KeycloakService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  async canActivate(): Promise<boolean> {
    console.log('AuthGuard: Checking if user is logged in...');

    const isLoggedIn = await this.keycloak.isLoggedIn();
    console.log('AuthGuard: isLoggedIn =', isLoggedIn);

    if (!isLoggedIn) {
      console.warn('AuthGuard: User not logged in. Redirecting to landing page.');
      this.snackBar.open('Please log in to continue', 'Close', {
        duration: 3000
      });
      this.router.navigate(['/landing']);
      return false;
    }

    console.log('AuthGuard: User is logged in. Allowing navigation.');
    return true;
  }
}
