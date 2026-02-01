import Keycloak from 'keycloak-js';

export const keycloak = new Keycloak({
  url: 'http://localhost:8181',
  realm: 'appverse',
  clientId: 'appverse-frontend',
});
