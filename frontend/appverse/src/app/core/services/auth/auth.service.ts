import { Injectable } from '@angular/core';
import { KeycloakInstance, KeycloakTokenParsed } from 'keycloak-js';
import { keycloak } from '../../../auth/keycloak';

interface ExtendedKeycloakToken extends KeycloakTokenParsed {
  preferred_username?: string;
  email?: string;
  given_name?: string;
  family_name?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  // private keycloakInstance: KeycloakInstance;

  // constructor(private keycloakService: KeycloakService) {
  //   this.keycloakInstance = this.keycloakService.getKeycloakInstance();
  // }

  get tokenParsed(): ExtendedKeycloakToken | undefined {
    return keycloak.tokenParsed as ExtendedKeycloakToken;
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




  async logout(): Promise<void> {
    await keycloak.logout({
      redirectUri: window.location.origin,
    })
  }

  isLoggedIn(): boolean {
    return keycloak.authenticated ?? false;
  }

  getToken(): string | undefined{
    return keycloak.token;
  }

  getUsername(): string | undefined {
    return this.tokenParsed?.preferred_username;
  }

  getDeveloperId(): string | null {
    return keycloak.subject || null;
  }

  getUserRoles(): string[] {
    return keycloak.realmAccess?.roles || [];
  }
}
