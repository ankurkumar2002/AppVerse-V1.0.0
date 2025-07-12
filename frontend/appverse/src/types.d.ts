// src/types.d.ts
import { KeycloakTokenParsed } from 'keycloak-js';

declare module 'keycloak-js' {
  interface KeycloakTokenParsed {
    preferred_username?: string;
    exp?: number;
    iat?: number;
    nonce?: string;
    sub?: string;
    session_state?: string;
    realm_access?: {
      roles: string[];
    };
    resource_access?: string[];
    // Add other custom claims if needed
  }
}