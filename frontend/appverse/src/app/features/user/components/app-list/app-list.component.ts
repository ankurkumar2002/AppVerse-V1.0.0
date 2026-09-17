import {
  Component,
  OnInit,
  OnDestroy
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

import { MatIconModule } from '@angular/material/icon';

import { ApplicationService } from '../../../application/services/application.service';
import { ApplicationResponse } from '../../../application/models/application-response';

import { CartService } from '../../../cart/services/cart.service';

import { CategoryService } from '../../../../core/services/categories/category.service';
import { Category } from '../../../../models/category';

@Component({
  selector: 'app-app-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    RouterModule
  ],
  templateUrl: './app-list.component.html',
  styleUrl: './app-list.component.scss'
})
export class AppListComponent implements OnInit, OnDestroy {

  applications: ApplicationResponse[] = [];

  filteredApplications: ApplicationResponse[] = [];

  categories: Category[] = [];

  isLoading = false;

  selectedCategoryId = '';

  searchTerm = '';

  sortOption = 'default';

  cartAppIds = new Set<string>();

  cartLoading: Record<string, boolean> = {};

  currentPage = 0;

  pageSize = 6;

  totalPages = 0;

  pages: number[] = [];

  imageUrls: Record<string, string> = {};

  imageLoading: Record<string, boolean> = {};

  imageLoadFailed: Record<string, boolean> = {};

  constructor(
    private applicationService: ApplicationService,
    private cartService: CartService,
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadApplications();
    this.loadCategories();
    this.loadCart();
  }

  loadApplications(): void {
    this.isLoading = true;

    this.applicationService
      .getPublushedApplications(
        this.currentPage,
        this.pageSize
      )
      .subscribe({
        next: (data) => {

          this.applications = [
            ...data.content
          ];

          this.totalPages =
            data.totalPages;

          this.pages = Array.from(
            {
              length: this.totalPages
            },
            (_, index) => index
          );

          this.applyFilters();

          this.applications.forEach(
            app => this.loadThumbnail(app)
          );

          this.isLoading = false;
        },

        error: (error) => {

          console.error(
            'Failed to load applications:',
            error
          );

          this.applications = [];

          this.filteredApplications = [];

          this.isLoading = false;
        }
      });
  }

  loadCategories(): void {

    this.categoryService
      .getAll()
      .subscribe({
        next: (categories) => {
          this.categories = categories;
        },

        error: (error) => {

          console.error(
            'Failed to load categories:',
            error
          );

          this.categories = [];
        }
      });
  }

  loadCart(): void {

    this.cartService
      .getCart()
      .subscribe({

        next: (cart) => {

          this.cartAppIds.clear();

          if (!cart?.items) {
            return;
          }

          cart.items.forEach(item => {

            this.cartAppIds.add(
              item.applicationId
            );
          });
        },

        error: (error) => {

          console.error(
            'Failed to load cart:',
            error
          );
        }
      });
  }

  applyFilters(): void {

    let result =
      [...this.applications];

    if (this.selectedCategoryId) {

      result = result.filter(
        app =>
          String(app.categoryId) ===
          String(this.selectedCategoryId)
      );
    }

    if (this.searchTerm) {

      const search =
        this.searchTerm.toLowerCase();

      result = result.filter(app => {

        const name =
          app.name?.toLowerCase() || '';

        const description =
          app.description?.toLowerCase() || '';

        const tagline =
          app.tagline?.toLowerCase() || '';

        const developer =
          app.developerName?.toLowerCase() || '';

        const category =
          app.categoryName?.toLowerCase() || '';

        const tags =
          app.tags?.join(' ')
            .toLowerCase() || '';

        return (
          name.includes(search) ||
          description.includes(search) ||
          tagline.includes(search) ||
          developer.includes(search) ||
          category.includes(search) ||
          tags.includes(search)
        );
      });
    }

    switch (this.sortOption) {

      case 'name-asc':

        result.sort((a, b) =>
          (a.name || '').localeCompare(
            b.name || ''
          )
        );

        break;

      case 'name-desc':

        result.sort((a, b) =>
          (b.name || '').localeCompare(
            a.name || ''
          )
        );

        break;

      case 'rating-high':

        result.sort((a, b) =>
          (b.averageRating || 0) -
          (a.averageRating || 0)
        );

        break;

      case 'rating-low':

        result.sort((a, b) =>
          (a.averageRating || 0) -
          (b.averageRating || 0)
        );

        break;

      case 'price-low':

        result.sort((a, b) =>
          (a.price || 0) -
          (b.price || 0)
        );

        break;

      case 'price-high':

        result.sort((a, b) =>
          (b.price || 0) -
          (a.price || 0)
        );

        break;

      default:
        break;
    }

    this.filteredApplications = result;
  }

  onSearch(event: Event): void {

    const input =
      event.target as HTMLInputElement;

    this.searchTerm =
      input.value
        .trim()
        .toLowerCase();

    this.applyFilters();
  }

  onCategoryChange(event: Event): void {

    const select =
      event.target as HTMLSelectElement;

    this.selectedCategoryId =
      select.value || '';

    this.applyFilters();
  }

  onSortChange(event: Event): void {

    const select =
      event.target as HTMLSelectElement;

    this.sortOption =
      select.value || 'default';

    this.applyFilters();
  }

  resetFilters(): void {

    this.searchTerm = '';

    this.selectedCategoryId = '';

    this.sortOption = 'default';

    this.applyFilters();
  }

  isFreeApplication(
    app: ApplicationResponse
  ): boolean {

    return app.monetizationType === 'FREE';
  }

  isPaidApplication(
    app: ApplicationResponse
  ): boolean {

    return (
      app.monetizationType === 'PAID' ||
      app.monetizationType === 'SUBSCRIPTION'
    );
  }

  hasAccessUrl(
    app: ApplicationResponse
  ): boolean {

    return !!app.accessUrl?.trim();
  }

  accessFreeApplication(
    app: ApplicationResponse
  ): void {

    const url =
      app.accessUrl?.trim();

    if (!url) {
      return;
    }

    window.open(
      url,
      '_blank',
      'noopener,noreferrer'
    );
  }

  addToCart(
    applicationId: string
  ): void {

    if (
      this.cartLoading[applicationId]
    ) {
      return;
    }

    this.cartLoading[applicationId] =
      true;

    this.cartService
      .addToCart({
        applicationId,
        quantity: 1
      })
      .subscribe({

        next: () => {

          this.cartAppIds.add(
            applicationId
          );

          this.cartLoading[applicationId] =
            false;
        },

        error: (error) => {

          console.error(
            'Failed to add item to cart:',
            error
          );

          this.cartLoading[applicationId] =
            false;
        }
      });
  }

  removeFromCart(
    applicationId: string
  ): void {

    if (
      this.cartLoading[applicationId]
    ) {
      return;
    }

    this.cartLoading[applicationId] =
      true;

    this.cartService
      .removeItemFromCart(applicationId)
      .subscribe({

        next: () => {

          this.cartAppIds.delete(
            applicationId
          );

          this.cartLoading[applicationId] =
            false;
        },

        error: (error) => {

          console.error(
            'Failed to remove item from cart:',
            error
          );

          this.cartLoading[applicationId] =
            false;
        }
      });
  }

  goToCart(): void {

    this.router.navigate([
      '/user/cart'
    ]);
  }

  loadThumbnail(
    app: ApplicationResponse
  ): void {

    this.imageLoading[app.id] = true;

    this.imageLoadFailed[app.id] = false;

    if (!app.thumbnailUrl) {

      this.imageLoading[app.id] = false;

      this.imageLoadFailed[app.id] = true;

      return;
    }

    const filename =
      app.thumbnailUrl
        .split(/[/\\]/)
        .pop();

    if (!filename) {

      this.imageLoading[app.id] = false;

      this.imageLoadFailed[app.id] = true;

      return;
    }

    this.applicationService
      .getImageAsBlob(
        'thumbnails',
        filename
      )
      .subscribe({

        next: (blob) => {

          if (this.imageUrls[app.id]) {
            URL.revokeObjectURL(
              this.imageUrls[app.id]
            );
          }

          this.imageUrls[app.id] =
            URL.createObjectURL(blob);

          this.imageLoading[app.id] =
            false;
        },

        error: (error) => {

          console.error(
            'Failed to load thumbnail:',
            error
          );

          this.imageLoading[app.id] =
            false;

          this.imageLoadFailed[app.id] =
            true;
        }
      });
  }

  onImageLoaded(
    appId: string
  ): void {

    this.imageLoading[appId] =
      false;

    this.imageLoadFailed[appId] =
      false;
  }

  onImageError(
    appId: string
  ): void {

    this.imageLoading[appId] =
      false;

    this.imageLoadFailed[appId] =
      true;

    const url =
      this.imageUrls[appId];

    if (url) {

      URL.revokeObjectURL(url);

      delete this.imageUrls[appId];
    }
  }

  getCategoryName(): string {

    const category =
      this.categories.find(
        category =>
          `${category.id}` ===
          `${this.selectedCategoryId}`
      );

    return category?.name ||
      'Category';
  }

  goToPage(page: number): void {

    if (
      page < 0 ||
      page >= this.totalPages
    ) {
      return;
    }

    this.currentPage = page;

    this.loadApplications();
  }

  nextPage(): void {

    if (
      this.currentPage <
      this.totalPages - 1
    ) {

      this.currentPage++;

      this.loadApplications();
    }
  }

  previousPage(): void {

    if (this.currentPage > 0) {

      this.currentPage--;

      this.loadApplications();
    }
  }

  ngOnDestroy(): void {

    Object
      .values(this.imageUrls)
      .forEach(url => {
        URL.revokeObjectURL(url);
      });
  }
}