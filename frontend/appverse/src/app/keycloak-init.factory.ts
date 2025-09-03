import { KeycloakService } from "keycloak-angular";

export function initializeKeycloak(keycloak: KeycloakService): () => Promise<boolean> {
  return () =>
    keycloak.init({
      config: {
        url: 'http://localhost:8181',       // Keycloak auth server URL
        realm: 'appverse',                 // Your realm
        clientId: 'appverse-angular-user', // Angular client
      },
      initOptions: {
        onLoad: 'check-sso',
        checkLoginIframe: false,
      },
      // ✅ move silentCheckSsoRedirectUri here (root level, not inside initOptions)
      silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
      enableBearerInterceptor: true,
      bearerPrefix: 'Bearer',
    } as any); // 👈 optional cast if TypeScript still complains
}
