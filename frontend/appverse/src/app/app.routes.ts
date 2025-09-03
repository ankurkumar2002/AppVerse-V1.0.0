import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';
import { DeveloperDashboardComponent } from './features/developer/pages/developer-dashboard/developer-dashboard.component';
import { DeveloperFormComponent } from './features/developer/pages/developer-form/developer-form.component';
import { DeveloperProfileUpdateComponent } from './features/developer/pages/developer-profile-update/developer-profile-update.component';
import { ApplicationComponent } from './features/application/application/application.component';
import { ApplicationCreateComponent } from './features/application/application-create/application-create.component';
import { ApplicationDetailComponent } from './features/application/application-detail/application-detail.component';
import { ApplicationUpdateComponent } from './features/application/application-update/application-update.component';
import { DeveloperAuthGuard } from './guards/developer-auth.guard';
import { LandingComponent } from './pages/landing/landing.component';
import { AboutComponent } from './pages/about/about.component';
import { ContactComponent } from './pages/contact/contact.component';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: 'landing', component: LandingComponent, title: 'AppVerse - Home' },
  { path: 'about', component: AboutComponent, title: 'About AppVerse' },
  { path: 'contact', component: ContactComponent, title: 'Contact Us' },
  { 
    path: 'developer/create', 
    component: DeveloperFormComponent, 
    title: 'Create Developer Profile', 
    canActivate: [AuthGuard] 
  },
  {
    path: 'developer',
    component: LayoutComponent,
    canActivate: [AuthGuard, DeveloperAuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { 
        path: 'dashboard', 
        component: DeveloperDashboardComponent, 
        title: 'Developer Dashboard',
        canActivate: [RoleGuard],
        data: { expectedRoles: ['developer'] }
      },
      { 
        path: 'update', 
        component: DeveloperProfileUpdateComponent, 
        title: 'Update Developer Profile',
        canActivate: [RoleGuard],
        data: { expectedRoles: ['developer'] }
      },
      {
        path: 'apps',
        canActivate: [RoleGuard],
        data: { expectedRoles: ['developer'] },
        children: [
          { path: '', component: ApplicationComponent, title: 'Applications' },
          { path: 'create', component: ApplicationCreateComponent, title: 'Create Application' },
          { path: ':id', component: ApplicationDetailComponent, title: 'Application Details' },
          { path: ':id/edit', component: ApplicationUpdateComponent, title: 'Update Application' }
        ]
      },
    ]
  },
  { path: '**', redirectTo: 'landing' }
];