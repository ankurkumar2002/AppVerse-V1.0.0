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
import { AppDetailComponent } from './features/user/components/app-detail/app-detail.component';
import { UserProfileComponent } from './features/user/components/user-personal-details/user-personal-details.component';
import { EditUserComponent } from './features/user/components/edit-user/edit-user.component';
import { EditPasswordComponent } from './features/user/components/edit-password/edit-password.component';
import { UpdatePersonelDetailsComponent } from './features/developer/components/update-personel-details/update-personel-details.component';
import { UpdatePasswordComponent } from './features/developer/components/update-password/update-password.component';


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
      { path: 'dashboard', component: DeveloperDashboardComponent, canActivate: [roleGuard(['DEVELOPER'])] },
      { path: 'update-personel-details', component: UpdatePersonelDetailsComponent, canActivate: [roleGuard(['DEVELOPER'])] },
      { path: 'update-password', component: UpdatePasswordComponent, canActivate: [roleGuard(['DEVELOPER'])] },

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


  {
    path: 'user',
    component: UserLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'profile-completion', component: ProfileCompletionComponent, canActivate: [profileCompletionGuard] },
      { path: 'dashboard', component: UserDashboardComponent, canActivate: [roleGuard(['USER'])] },
      { path: 'apps', component: AppListComponent, canActivate: [roleGuard(['USER'])] },
      { path: 'apps/:id', component: AppDetailComponent, canActivate: [roleGuard(['USER'])] },
      { path: 'profile', component: UserProfileComponent, canActivate: [roleGuard(['USER'])] },
      { path: 'edit-personal-details', component: EditUserComponent, canActivate: [roleGuard(['USER'])] },
      { path: 'update-password', component: EditPasswordComponent, canActivate: [roleGuard(['USER'])] }
    ]
  },
  // FALLBACK
  { path: '**', redirectTo: 'landing' }
];