import { Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakInstance, KeycloakTokenParsed } from 'keycloak-js';

interface ExtendedKeycloakToken extends KeycloakTokenParsed {
  preferred_username?: string;
  email?: string;
  given_name?: string;
  family_name?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private keycloakInstance: KeycloakInstance;

  constructor(private keycloakService: KeycloakService) {
    this.keycloakInstance = this.keycloakService.getKeycloakInstance();
  }

  get tokenParsed(): ExtendedKeycloakToken | undefined {
    return this.keycloakInstance.tokenParsed as ExtendedKeycloakToken;
  }

  /**
   * Login depending on role (user or developer)
   */
  async login(role: 'user' | 'developer'): Promise<void> {
  const clientId = role === 'developer' ? 'developer-client-frontend' : 'user-client-frontend';
  const redirectUri =
    role === 'developer'
      ? `${window.location.origin}/developer/dashboard`
      : `${window.location.origin}/user/dashboard`;

  const url = `http://localhost:8181/realms/appverse/protocol/openid-connect/auth?` +
    `client_id=${clientId}` +   // 👈 use role-specific clientId
    `&redirect_uri=${encodeURIComponent(redirectUri)}` +
    `&response_type=code&scope=openid`;

  window.location.href = url;
}

async register(role: 'user' | 'developer'): Promise<void> {
  const clientId = role === 'developer' ? 'developer-client-frontend' : 'user-client-frontend';
  const redirectUri =
    role === 'developer'
      ? `${window.location.origin}/developer/create`
      : `${window.location.origin}/post-register?mode=${role}`;

  const url = `http://localhost:8181/realms/appverse/protocol/openid-connect/registrations?` +
    `client_id=${clientId}` +   // 👈 role-specific clientId
    `&redirect_uri=${encodeURIComponent(redirectUri)}` +
    `&response_type=code&scope=openid`;

  window.location.href = url;
}




  logout(): void {
    this.keycloakService.logout(window.location.origin);
  }

  isLoggedIn(): Promise<boolean> {
    return Promise.resolve(this.keycloakService.isLoggedIn());
  }

  getToken(): Promise<string> {
    return this.keycloakService.getToken();
  }

  getUsername(): string | undefined {
    return this.tokenParsed?.preferred_username;
  }

  getDeveloperId(): string | null {
    return this.keycloakInstance.subject || null;
  }

  getUserRoles(): string[] {
    return this.keycloakInstance.realmAccess?.roles || [];
  }
}
