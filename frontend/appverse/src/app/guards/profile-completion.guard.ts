import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { UserAuthService } from '../core/services/user/user-auth.service';
import { KeycloakService } from 'keycloak-angular';

@Injectable({
  providedIn: 'root'
})
export class ProfileCompletionGuard implements CanActivate {
  constructor(
    private userAuthService: UserAuthService,
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  async canActivate(): Promise<boolean> {
  const profile = await this.keycloakService.loadUserProfile();
  const keycloakId = profile.id;

  if (!keycloakId) {
    this.router.navigate(['/profile-completion']);
    return false;
  }

  return new Promise((resolve) => {
    this.userAuthService.getUserByKeycloakId(keycloakId).pipe(
      map((user) => {
        if (user && user.username) {
          return true; // ✅ Profile exists
        } else {
          this.router.navigate(['/user/profile-completion']);
          return false;
        }
      }),
      catchError(() => {
        this.router.navigate(['/user/profile-completion']);
        return of(false);
      })
    ).subscribe(resolve);
  });
}

}
