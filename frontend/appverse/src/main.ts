import { bootstrapApplication } from '@angular/platform-browser';
import { APP_INITIALIZER } from '@angular/core';
import {
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { initializeKeycloak } from './app/auth/keycloak-init.factory';
import { authInterceptor } from './app/auth/auth.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideAnimations(),

    // 🔐 Keycloak initialization (silent SSO)
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
    },

    // 🔐 Token attachment
    provideHttpClient(
      withInterceptors([authInterceptor])
    ),
  ],
}).catch(err => console.error(err));
