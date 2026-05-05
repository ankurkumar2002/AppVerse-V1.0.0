import {
  createKeycloak,
  setActiveKeycloak
} from './keycloak';

export function initializeKeycloak(): () => Promise<boolean> {
  return async () => {
    const clientId = localStorage.getItem('auth_client');

    if (!clientId) {
      return false;
    }

    const kc = createKeycloak(clientId);

    const authenticated = await kc.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri:
        window.location.origin + '/assets/silent-check-sso.html',
      checkLoginIframe: false
    });

    setActiveKeycloak(kc);

    return authenticated;
  };
}