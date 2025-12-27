import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/developer-layout/layout.component';
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
// import { SubscriptionsComponent } from './features/user/pages/subscriptions/subscriptions.component';
// import { ProfileComponent } from './features/user/pages/profile/profile.component';

import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { UserLayoutComponent } from './layout/user-layout/user-layout.component';
import { ProfileCompletionGuard } from './guards/profile-completion.guard';
import { ProfileCompletionComponent } from './features/user/pages/profile-completion/profile-completion.component';

export const routes: Routes = [
  { path: 'landing', component: LandingComponent, title: 'AppVerse - Home' },
  { path: 'about', component: AboutComponent, title: 'About AppVerse' },
  { path: 'contact', component: ContactComponent, title: 'Contact Us' },

  // 👉 Developer routes
  {
    path: 'developer',
    component: LayoutComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { expectedRoles: ['developer'] },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DeveloperDashboardComponent, title: 'Developer Dashboard' },
      { path: 'update', component: DeveloperProfileUpdateComponent, title: 'Update Developer Profile' },
      {
        path: 'apps',
        children: [
          { path: '', component: ApplicationComponent, title: 'Applications' },
          { path: 'create', component: ApplicationCreateComponent, title: 'Create Application' },
          { path: ':id', component: ApplicationDetailComponent, title: 'Application Details' },
          { path: ':id/edit', component: ApplicationUpdateComponent, title: 'Update Application' }
        ]
      },
    ]
  },
{
  path: 'user',
  component: UserLayoutComponent,
  canActivate: [AuthGuard], 
  data: { expectedRoles: ['user'] },
  children: [
    { path: 'profile-completion', component: ProfileCompletionComponent, title: 'Complete Profile' },

    // All other pages should be blocked until profile is complete
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    { path: 'dashboard', component: UserDashboardComponent, title: 'User Dashboard', canActivate: [ProfileCompletionGuard, RoleGuard], data: { expectedRoles: ['user']} },
    { path: 'apps', component: AppListComponent, title: 'Browse Apps', canActivate: [ProfileCompletionGuard, RoleGuard],data: { expectedRoles: ['user'] }},
    // { path: 'subscriptions', component: SubscriptionsComponent, title: 'Subscriptions', canActivate: [ProfileCompletionGuard] },
  ]
}
,

  { path: '**', redirectTo: 'landing' }
];
