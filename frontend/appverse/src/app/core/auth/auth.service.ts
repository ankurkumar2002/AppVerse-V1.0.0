import { Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private keycloakService: KeycloakService) {}

  isLoggedIn(): Promise<boolean> {
    return Promise.resolve(this.keycloakService.isLoggedIn());
  }

  login(): void {
    this.keycloakService.login();
  }

  logout(): void {
    this.keycloakService.logout(window.location.origin);
  }

  getToken(): Promise<string> {
    return this.keycloakService.getToken();
  }

  getUsername(): string | undefined {
    const tokenParsed = this.keycloakService.getKeycloakInstance().tokenParsed as any;
    return tokenParsed?.preferred_username;
  }

  getDeveloperId(): string | null {
    const tokenParsed = this.keycloakService.getKeycloakInstance().tokenParsed as any;
    return tokenParsed?.sub || null;
  }

  getUserRoles(): string[] {
    const tokenParsed = this.keycloakService.getKeycloakInstance().tokenParsed;
    return tokenParsed?.realm_access?.roles || [];
  }
}
