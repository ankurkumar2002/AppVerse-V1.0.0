import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

import { MatIconModule } from '@angular/material/icon';

import { ApplicationService } from '../../../application/services/application.service';
import { CategoryService } from '../../../../core/services/categories/category.service';

import { ApplicationResponse } from '../../../application/models/application-response';
import { Category } from '../../../../models/category';

import { getKeycloak } from '../../../../core/auth/keycloak';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,

  imports: [
    CommonModule,
    MatIconModule
  ],

  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.scss']
})
export class UserDashboardComponent implements OnInit {

  // ============================================================
  // USER
  // ============================================================

  username = 'User';


  // ============================================================
  // APPLICATIONS
  // ============================================================

  applications:
    (ApplicationResponse & {
      thumbnailBlobUrl?: SafeUrl
    })[] = [];


  // ============================================================
  // CATEGORIES
  // ============================================================

  categories: Category[] = [];


  // ============================================================
  // LOADING
  // ============================================================

  loadingApps = true;

  loadingCategories = true;


  // ============================================================
  // CONSTRUCTOR
  // ============================================================

  constructor(
    private appService: ApplicationService,
    private categoryService: CategoryService,
    private sanitizer: DomSanitizer,
    private router: Router
  ) {}


  // ============================================================
  // INITIALIZATION
  // ============================================================

  async ngOnInit(): Promise<void> {

    await this.loadUser();

    this.loadApplications();

    this.loadCategories();
  }


  // ============================================================
  // LOAD USER
  // ============================================================

  private async loadUser(): Promise<void> {

    try {

      const profile =
        await getKeycloak().loadUserProfile();

      this.username =
        profile.firstName ??
        profile.username ??
        'User';

    } catch (error) {

      console.error(
        'Failed to load Keycloak profile:',
        error
      );

      this.username = 'User';
    }
  }


  // ============================================================
  // LOAD APPLICATIONS
  // ============================================================

  private loadApplications(): void {

    this.loadingApps = true;

    this.appService
      .getAllApplications()
      .subscribe({

        next: apps => {

          this.applications =
            apps.slice(0, 5);

          this.loadingApps = false;
        },

        error: error => {

          console.error(
            'Failed to load applications:',
            error
          );

          this.applications = [];

          this.loadingApps = false;
        }

      });
  }


  // ============================================================
  // LOAD CATEGORIES
  // ============================================================

  private loadCategories(): void {

    this.loadingCategories = true;

    this.categoryService
      .getAll()
      .subscribe({

        next: cats => {

          this.categories = cats;

          this.loadingCategories = false;
        },

        error: error => {

          console.error(
            'Failed to load categories:',
            error
          );

          this.categories = [];

          this.loadingCategories = false;
        }

      });
  }


  // ============================================================
  // NAVIGATION
  // ============================================================

  goToApps(): void {

    this.router.navigate([
      '/apps'
    ]);
  }


  goToProfile(): void {

    this.router.navigate([
      '/user/profile-completion'
    ]);
  }


  goToDashboard(): void {

    this.router.navigate([
      '/user/dashboard'
    ]);
  }


  goToApp(appId: string): void {

    this.router.navigate([
      '/apps',
      appId
    ]);
  }


  goToCategory(categoryId: string): void {

    this.router.navigate(
      ['/apps'],
      {
        queryParams: {
          category: categoryId
        }
      }
    );
  }

}