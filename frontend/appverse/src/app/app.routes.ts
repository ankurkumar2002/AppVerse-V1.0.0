import { Routes } from '@angular/router';

import { LandingComponent } from './pages/landing/landing.component';
import { AboutComponent } from './pages/about/about.component';
import { ContactComponent } from './pages/contact/contact.component';



import { LayoutComponent } from './layout/developer-layout/layout.component';
import { UserLayoutComponent } from './layout/user-layout/user-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { profileCompletionGuard } from './core/guards/profile-completion.guard';
import { roleGuard } from './core/guards/role.guard';
import { ApplicationCreateComponent } from './features/application/components/application-create/application-create.component';
import { ApplicationDetailComponent } from './features/application/components/application-detail/application-detail.component';
import { ApplicationUpdateComponent } from './features/application/components/application-update/application-update.component';
import { ApplicationComponent } from './features/application/components/application/application.component';
import { DeveloperDashboardComponent } from './features/developer/components/developer-dashboard/developer-dashboard.component';
import { DeveloperFormComponent } from './features/developer/components/developer-form/developer-form.component';
import { DeveloperProfileUpdateComponent } from './features/developer/components/developer-profile-update/developer-profile-update.component';
import { AppListComponent } from './features/user/components/app-list/app-list.component';
import { ProfileCompletionComponent } from './features/user/components/profile-completion/profile-completion.component';
import { UserDashboardComponent } from './features/user/components/user-dashboard/user-dashboard.component';


export const routes: Routes = [
  // PUBLIC
  { path: '', redirectTo: 'landing', pathMatch: 'full' },
  { path: 'landing', component: LandingComponent },
  { path: 'about', component: AboutComponent },
  { path: 'contact', component: ContactComponent },

  // DEVELOPER
  {
    path: 'developer',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'create', pathMatch: 'full' },

      // Onboarding/Profile routes → token only
      { path: 'create', component: DeveloperFormComponent },
      { path: 'update', component: DeveloperProfileUpdateComponent },

      // Protected developer-only routes
      {
        path: 'dashboard',
        component: DeveloperDashboardComponent,
        canActivate: [roleGuard(['DEVELOPER'])]
      },
      {
        path: 'apps',
        canActivate: [roleGuard(['DEVELOPER'])],
        children: [
          { path: '', component: ApplicationComponent },
          { path: 'create', component: ApplicationCreateComponent },
          { path: ':id', component: ApplicationDetailComponent },
          { path: ':id/edit', component: ApplicationUpdateComponent }
        ]
      }
    ]
  },

  // USER
  // USER
// USER
{
  path: 'user',
  component: UserLayoutComponent,
  canActivate: [authGuard],
  children: [

    {
      path: '',
      redirectTo: 'dashboard',
      pathMatch: 'full'
    },

    {
      path: 'profile-completion',
      component: ProfileCompletionComponent,
      canActivate: [profileCompletionGuard]
    },

    {
      path: 'dashboard',
      component: UserDashboardComponent,
      canActivate: [roleGuard(['USER'])]
    },

    {
      path: 'apps',
      component: AppListComponent,
      canActivate: [roleGuard(['USER'])]
    }
  ]
},
  // FALLBACK
  { path: '**', redirectTo: 'landing' }
];