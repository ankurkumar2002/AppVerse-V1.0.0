import Keycloak, { KeycloakInstance } from 'keycloak-js';

let activeKeycloak: KeycloakInstance | null = null;

export function createKeycloak(clientId: string): KeycloakInstance {
  return new Keycloak({
    url: 'http://localhost:8181',
    realm: 'appverse',
    clientId
  });
}

export function setActiveKeycloak(instance: KeycloakInstance): void {
  activeKeycloak = instance;
}

export function getKeycloak(): KeycloakInstance {
  if (!activeKeycloak) {
    throw new Error('Keycloak has not been initialized.');
  }

  return activeKeycloak;
}