import { Component, OnInit } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AsyncPipe, DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { DeveloperService } from '../../services/developer.service';

@Component({
  selector: 'app-developer-dashboard',
  standalone: true,

  imports: [
    MatIconModule,
    MatProgressSpinnerModule,
    AsyncPipe,
    DatePipe,
    RouterLink
  ],

  templateUrl: './developer-dashboard.component.html',
  styleUrls: ['./developer-dashboard.component.scss']
})
export class DeveloperDashboardComponent implements OnInit {

  developer$ = this.developerService.profile$;

  isLoading = true;


  /* ================================================================
     STATISTICS
  ================================================================ */

  stats = [
    {
      icon: 'apps',
      value: 5,
      label: 'Published Apps',
      description: 'Applications currently available'
    },

    {
      icon: 'people',
      value: 128,
      label: 'Active Users',
      description: 'Users interacting with your apps'
    },

    {
      icon: 'star',
      value: 4.8,
      label: 'Average Rating',
      description: 'Average rating from users'
    },

    {
      icon: 'download',
      value: '2.4K',
      label: 'Total Downloads',
      description: 'Total application downloads'
    }
  ];


  /* ================================================================
     QUICK ACTIONS
  ================================================================ */

  quickActions = [

    {
      icon: 'cloud_upload',
      title: 'Upload App',
      description: 'Publish a new application',
      route: '/developer/apps/create'
    },

    {
      icon: 'apps',
      title: 'Manage Apps',
      description: 'View and manage your applications',
      route: '/developer/apps'
    },

    {
      icon: 'edit',
      title: 'Edit Profile',
      description: 'Update your developer information',
      route: '/developer/update'
    },

    {
      icon: 'analytics',
      title: 'View Analytics',
      description: 'Track application performance',
      route: '/developer/analytics'
    }

  ];


  /* ================================================================
     RECENT ACTIVITY
  ================================================================ */

  recentActivities = [

    {
      icon: 'cloud_upload',
      message: 'New application published',
      description: 'Your latest application was successfully published.',
      date: new Date()
    },

    {
      icon: 'edit',
      message: 'Profile updated',
      description: 'Your developer profile information was updated.',
      date: new Date(Date.now() - 86400000)
    },

    {
      icon: 'people',
      message: 'New users discovered your app',
      description: 'Your applications received new users.',
      date: new Date(Date.now() - 2 * 86400000)
    }

  ];


  /* ================================================================
     DEVELOPER TIPS
  ================================================================ */

  tips = [

    {
      icon: 'rocket_launch',
      title: 'Keep your app updated',
      description:
        'Regular updates help improve user experience and keep your application relevant.'
    },

    {
      icon: 'star',
      title: 'Focus on quality',
      description:
        'A polished application with good documentation is more likely to receive positive ratings.'
    },

    {
      icon: 'analytics',
      title: 'Watch your analytics',
      description:
        'Keep an eye on downloads and user activity to understand how your application is performing.'
    }

  ];


  constructor(
    private developerService: DeveloperService,
    private router: Router
  ) {}


  ngOnInit(): void {

    this.developerService
      .getMyDeveloperProfile()
      .subscribe({

        next: () => {
          this.isLoading = false;
        },

        error: (error) => {

          console.error(
            'Failed to load developer profile',
            error
          );

          this.isLoading = false;
        }

      });

  }


  /* ================================================================
     EDIT PROFILE
  ================================================================ */

  editProfile(): void {

    this.router.navigate([
      '/developer/update'
    ]);

  }

}