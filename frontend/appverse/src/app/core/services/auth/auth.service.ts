import { Injectable } from '@angular/core';
import { KeycloakTokenParsed } from 'keycloak-js';
import { getKeycloak } from '../../auth/keycloak';

interface ExtendedKeycloakToken extends KeycloakTokenParsed {
  preferred_username?: string;
  email?: string;
  given_name?: string;
  family_name?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  get tokenParsed(): ExtendedKeycloakToken | undefined {
    return getKeycloak().tokenParsed as ExtendedKeycloakToken;
  }

  async login(role: 'user' | 'developer'): Promise<void> {
    const clientId =
      role === 'developer'
        ? 'developer-client-frontend'
        : 'user-client-frontend';

    localStorage.setItem('auth_client', clientId);

    const redirectUri =
      role === 'developer'
        ? `${window.location.origin}/developer/dashboard`
        : `${window.location.origin}/user/dashboard`;

    const authUrl =
      `http://localhost:8181/realms/appverse/protocol/openid-connect/auth` +
      `?client_id=${clientId}` +
      `&redirect_uri=${encodeURIComponent(redirectUri)}` +
      `&response_type=code` +
      `&scope=openid`;

    window.location.href = authUrl;
  }

  async register(role: 'user' | 'developer'): Promise<void> {
    const clientId =
      role === 'developer'
        ? 'developer-client-frontend'
        : 'user-client-frontend';

    localStorage.setItem('auth_client', clientId);

    const redirectUri =
      role === 'developer'
        ? `${window.location.origin}/developer/create`
        : `${window.location.origin}/user/profile-completion`;

    const authUrl =
      `http://localhost:8181/realms/appverse/protocol/openid-connect/auth` +
      `?client_id=${clientId}` +
      `&redirect_uri=${encodeURIComponent(redirectUri)}` +
      `&response_type=code` +
      `&scope=openid` +
      `&register=true`;

    window.location.href = authUrl;
  }

  async logout(): Promise<void> {
    await getKeycloak().logout({
      redirectUri: window.location.origin
    });
  }

  isLoggedIn(): boolean {
    return getKeycloak().authenticated ?? false;
  }

  getToken(): string | undefined {
    return getKeycloak().token;
  }

  getUsername(): string | undefined {
    return this.tokenParsed?.preferred_username;
  }

  getDeveloperId(): string | null {
    return getKeycloak().subject || null;
  }

  getUserRoles(): string[] {
    return getKeycloak().realmAccess?.roles || [];
  }
}