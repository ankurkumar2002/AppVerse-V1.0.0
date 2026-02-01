import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

import { ApplicationService } from '../../../../core/services/application/application.service';
import { CategoryService } from '../../../../core/services/categories/category.service';
import { ApplicationResponse } from '../../../../models/application-response';
import { Category } from '../../../../models/category';
import { keycloak } from '../../../../auth/keycloak';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.scss']
})
export class UserDashboardComponent implements OnInit {
  username = '';
  applications: (ApplicationResponse & { thumbnailBlobUrl?: SafeUrl })[] = [];
  categories: Category[] = [];

  loadingApps = true;
  loadingCategories = true;

  constructor(
    private appService: ApplicationService,
    private categoryService: CategoryService,
    private sanitizer: DomSanitizer,
    private router: Router
  ) {}

  async ngOnInit(): Promise<void> {
    const profile = await keycloak.loadUserProfile();
    this.username = profile.firstName ?? profile.username ?? 'User';

    this.appService.getAllApplications().subscribe(apps => {
      this.applications = apps.slice(0, 5);
      this.loadingApps = false;
    });

    this.categoryService.getAll().subscribe(cats => {
      this.categories = cats;
      this.loadingCategories = false;
    });
  }

  goToApps(): void {
    this.router.navigate(['/apps']);
  }

  goToProfile(): void {
    this.router.navigate(['/user/profile-completion']);
  }

  goToDashboard(): void {
    this.router.navigate(['/user/dashboard']);
  }

  goToApp(appId: string): void {
    this.router.navigate(['/apps', appId]);
  }

  goToCategory(categoryId: string): void {
    this.router.navigate(['/apps'], { queryParams: { category: categoryId } });
  }
}
