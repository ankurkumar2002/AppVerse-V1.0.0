import { keycloak } from "./keycloak";

export function initializeKeycloak(): () => Promise<boolean> {
  return () =>
    keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri:
        window.location.origin + '/assets/silent-check-sso.html',
      checkLoginIframe: false,
    });
}
