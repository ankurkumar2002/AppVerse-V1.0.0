import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';
import { DeveloperDashboardComponent } from './pages/developer-dashboard/developer-dashboard.component';
import { DeveloperFormComponent } from './pages/developer-form/developer-form.component';
import { DeveloperProfileUpdateComponent } from './components/developer-profile-update/developer-profile-update.component';
import { ApplicationComponent } from './components/application/application.component';
import { ApplicationCreateComponent } from './components/application-create/application-create.component';
import { DeveloperAuthGuard } from './guards/developer-auth.guard';
import { ApplicationDetailComponent } from './components/application-detail/application-detail.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    canActivate: [DeveloperAuthGuard],
    children: [
      {
        path: '',
        redirectTo: 'developer/dashboard',
        pathMatch: 'full'
      },
      {
        path: 'developer',
        children: [
          {
            path: 'dashboard',
            component: DeveloperDashboardComponent,
            title: 'Developer Dashboard'
          },
          {
            path: 'create',
            component: DeveloperFormComponent,
            title: 'Create Developer'
          },
          {
            path: 'update',
            component: DeveloperProfileUpdateComponent,
            title: 'Update Developer Profile'
          }
        ]
      },
      {
        path: 'apps',
        children: [
          {
            path: '',
            component: ApplicationComponent,
            title: 'Applications'
          },
          {
            path: 'create',
            component: ApplicationCreateComponent,
            title: 'Create Application'
          },
          {
            path: ':id',
            component: ApplicationDetailComponent,
            title: 'Application Details'
          }
        ]
      },
      {
        path: '**',
        redirectTo: 'developer/dashboard'
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
