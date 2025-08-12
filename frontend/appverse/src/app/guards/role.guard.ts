import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  constructor(private keycloak: KeycloakService, private router: Router) {}

  async canActivate(route: ActivatedRouteSnapshot): Promise<boolean> {
    console.log('RoleGuard: Checking roles...');

    const isLoggedIn = await this.keycloak.isLoggedIn();
    console.log('RoleGuard: isLoggedIn =', isLoggedIn);

    if (!isLoggedIn) {
      console.warn('RoleGuard: Not logged in → redirecting');
      this.router.navigate(['/landing']);
      return false;
    }

    const expectedRoles = route.data['expectedRoles'] || [];
    console.log('RoleGuard: expectedRoles =', expectedRoles);

    const userRoles = this.keycloak.getUserRoles();
    console.log('RoleGuard: userRoles from Keycloak =', userRoles);

    const hasRole: boolean = expectedRoles.some((role: string) =>
  userRoles.map((r: string) => r.toLowerCase()).includes(role.toLowerCase())
);

    console.log('RoleGuard: hasRole =', hasRole);

    if (!hasRole) {
      console.warn('RoleGuard: User missing required role → redirecting');
      this.router.navigate(['/landing']);
      return false;
    }

    console.log('RoleGuard: Role check passed ✅');
    return true;
  }
}
