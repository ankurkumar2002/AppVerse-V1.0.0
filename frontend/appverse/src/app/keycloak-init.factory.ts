import { KeycloakService } from 'keycloak-angular';

export function initializeKeycloak(keycloak: KeycloakService): () => Promise<boolean> {
  return () =>
    keycloak.init({
      config: {
        url: 'http://localhost:8181',
        realm: 'appverse',
        clientId: 'appverse-angular',
      },
      initOptions: {
        onLoad: 'check-sso', // 👈 allows unauthenticated users
        checkLoginIframe: false,
      },
      enableBearerInterceptor: true,
      bearerPrefix: 'Bearer',
    });
}
