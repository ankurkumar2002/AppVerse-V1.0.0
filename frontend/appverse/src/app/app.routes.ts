import { Routes } from '@angular/router';

import { LandingComponent } from './pages/landing/landing.component';
import { AboutComponent } from './pages/about/about.component';
import { ContactComponent } from './pages/contact/contact.component';

import { DeveloperDashboardComponent } from './features/developer/pages/developer-dashboard/developer-dashboard.component';
import { DeveloperFormComponent } from './features/developer/pages/developer-form/developer-form.component';
import { DeveloperProfileUpdateComponent } from './features/developer/pages/developer-profile-update/developer-profile-update.component';

import { ApplicationComponent } from './features/application/application/application.component';
import { ApplicationCreateComponent } from './features/application/application-create/application-create.component';
import { ApplicationDetailComponent } from './features/application/application-detail/application-detail.component';
import { ApplicationUpdateComponent } from './features/application/application-update/application-update.component';

import { UserDashboardComponent } from './features/user/pages/user-dashboard/user-dashboard.component';
import { AppListComponent } from './features/user/pages/app-list/app-list.component';
import { ProfileCompletionComponent } from './features/user/pages/profile-completion/profile-completion.component';

import { LayoutComponent } from './layout/developer-layout/layout.component';
import { UserLayoutComponent } from './layout/user-layout/user-layout.component';

import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';
import { profileCompletionGuard } from './guards/profile-completion.guard';

export const routes: Routes = [
  // 🌍 PUBLIC ROUTES
  { path: '', redirectTo: 'landing', pathMatch: 'full' },
  { path: 'landing', component: LandingComponent },
  { path: 'about', component: AboutComponent },
  { path: 'contact', component: ContactComponent },

  // 👨‍💻 DEVELOPER (login + role)
  {
    path: 'developer',
    component: LayoutComponent,
    canActivate: [authGuard, roleGuard(['DEVELOPER'])],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DeveloperDashboardComponent },
      { path: 'update', component: DeveloperProfileUpdateComponent },
      {
        path: 'apps',
        children: [
          { path: '', component: ApplicationComponent },
          { path: 'create', component: ApplicationCreateComponent },
          { path: ':id', component: ApplicationDetailComponent },
          { path: ':id/edit', component: ApplicationUpdateComponent }
        ]
      }
    ]
  },

  // 👤 USER (login required)
  {
    path: 'user',
    component: UserLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'profile-completion', component: ProfileCompletionComponent },

      {
        path: 'dashboard',
        component: UserDashboardComponent,
        canActivate: [profileCompletionGuard, roleGuard(['USER'])]
      },
      {
        path: 'apps',
        component: AppListComponent,
        canActivate: [profileCompletionGuard, roleGuard(['USER'])]
      }
    ]
  },

  // ❓ FALLBACK
  { path: '**', redirectTo: 'landing' }
];
