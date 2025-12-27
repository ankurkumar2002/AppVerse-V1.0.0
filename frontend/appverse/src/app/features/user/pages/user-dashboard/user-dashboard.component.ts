import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApplicationService } from '../../../../core/services/application/application.service';
import { CategoryService } from '../../../../core/services/categories/category.service';
import { ApplicationResponse } from '../../../../models/application-response';
import { Category } from '../../../../models/category';
import { KeycloakService } from 'keycloak-angular';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

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
    private keycloak: KeycloakService,
    private sanitizer: DomSanitizer,
    public router: Router
  ) {}

  async ngOnInit() {
    // Load user profile from Keycloak
    const profile = await this.keycloak.loadUserProfile();
    this.username = profile.firstName || profile.username || 'User';

    // Load applications
    this.appService.getAllApplications().subscribe({
      next: (apps) => {
        const topApps = apps.slice(0, 5);
        this.applications = topApps;

        // Fetch thumbnails as blob for each app
        this.applications.forEach((app) => {
          if (app.thumbnailUrl) {
            const filename = this.extractFileName(app.thumbnailUrl);
            this.appService.getImageAsBlob('thumbnails', filename).subscribe({
              next: (blob) => {
                const objectURL = URL.createObjectURL(blob);
                app.thumbnailBlobUrl = this.sanitizer.bypassSecurityTrustUrl(objectURL);
              },
              error: (err) => console.error(`Error fetching thumbnail for ${app.name}:`, err)
            });
          }
        });

        this.loadingApps = false;
      },
      error: (err) => {
        console.error('Error loading apps:', err);
        this.loadingApps = false;
      }
    });

    // Load categories
    this.categoryService.getAll().subscribe({
      next: (cats) => {
        this.categories = cats;
        this.loadingCategories = false;
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.loadingCategories = false;
      }
    });
  }

  goToCategory(categoryId: string) {
    this.router.navigate(['/apps'], { queryParams: { category: categoryId } });
  }

  goToApp(appId: string) {
    this.router.navigate(['/apps', appId]);
  }

  extractFileName(fullPath: string): string {
    if (!fullPath) return '';
    return fullPath.split('/').pop() || '';
  }
}
