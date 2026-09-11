import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
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

  displayedColumns = [
    'name',
    'description',
    'version',
    'categoryId',
    'currency',
    'price',
    'platforms',
    'accessUrl',
    'websiteUrl'
  ];

  dataSource = new MatTableDataSource<ApplicationResponse>();

  isLoading = false;

  @ViewChild(MatPaginator)
  paginator!: MatPaginator;

  @ViewChild(MatSort)
  sort!: MatSort;

  allApplications: ApplicationResponse[] = [];

  categories: Category[] = [];

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
    private appService: ApplicationService,
    private cartService: CartService,
    private categoryService: CategoryService
  ) { }

  ngOnInit(): void {
    this.loadApplications();
    this.loadCategories();
    this.loadCart();
  }

  loadApplications(): void {
    this.isLoading = true;

    this.appService
      .getPublushedApplications(
        this.currentPage,
        this.pageSize
      )
      .subscribe({
        next: (data) => {
          this.allApplications = [...data.content];

          this.totalPages = data.totalPages;

          this.pages = Array.from(
            { length: this.totalPages },
            (_, index) => index
          );

          this.applyFilters();

          data.content.forEach(app => {
            this.loadThumbnail(app);
          });

          this.isLoading = false;
        },

        error: (err) => {
          console.error(
            'Failed to load applications:',
            err
          );

          this.allApplications = [];
          this.dataSource.data = [];
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

        error: (err) => {
          console.error(
            'Failed to load categories:',
            err
          );

          this.categories = [];
        }
      });
  }

  onCategoryChange(event: Event): void {
    const select =
      event.target as HTMLSelectElement;

    this.selectedCategoryId =
      select.value || '';

    this.applyFilters();
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

  onSortChange(event: Event): void {
    const select =
      event.target as HTMLSelectElement;

    this.sortOption =
      select.value || 'default';

    this.applyFilters();
  }

  applyFilters(): void {
    let result =
      [...this.allApplications];

    if (this.selectedCategoryId) {
      result =
        result.filter(app =>
          String(app.categoryId) ===
          String(this.selectedCategoryId)
        );
    }

    if (this.searchTerm) {
      result =
        result.filter(app => {

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
            app.tags
              ?.join(' ')
              .toLowerCase() || '';

          return (
            name.includes(this.searchTerm) ||
            description.includes(this.searchTerm) ||
            tagline.includes(this.searchTerm) ||
            developer.includes(this.searchTerm) ||
            category.includes(this.searchTerm) ||
            tags.includes(this.searchTerm)
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

    this.dataSource.data = result;

    if (this.paginator) {
      this.dataSource.paginator =
        this.paginator;
    }

    if (this.sort) {
      this.dataSource.sort =
        this.sort;
    }
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.selectedCategoryId = '';
    this.sortOption = 'default';

    this.applyFilters();
  }

  isPaidApplication(
    app: ApplicationResponse
  ): boolean {
    return Number(app.price || 0) > 0;
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

  getThumbnailUrl(
    path?: string
  ): string {
    if (!path) {
      return '';
    }

    const filename =
      path
        .split(/[/\\]/)
        .pop();

    if (!filename) {
      return '';
    }

    return `http://localhost:9000/api/apps/images/thumbnails/${filename}`;
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

    this.appService
      .getImageAsBlob(
        'thumbnails',
        filename
      )
      .subscribe({
        next: (blob) => {

          const objectUrl =
            URL.createObjectURL(blob);

          this.imageUrls[app.id] =
            objectUrl;

          this.imageLoading[app.id] = false;
        },

        error: (err) => {
          console.error(
            'Failed to load thumbnail:',
            err
          );

          this.imageLoading[app.id] = false;
          this.imageLoadFailed[app.id] = true;
        }
      });
  }

  onImageLoaded(
    appId: string
  ): void {
    this.imageLoading[appId] = false;
    this.imageLoadFailed[appId] = false;
  }

  onImageError(
    appId: string
  ): void {
    this.imageLoading[appId] = false;
    this.imageLoadFailed[appId] = true;

    const url =
      this.imageUrls[appId];

    if (url) {
      URL.revokeObjectURL(url);
      delete this.imageUrls[appId];
    }
  }

  loadCart(): void {
    this.cartService
      .getCart()
      .subscribe({
        next: (cart) => {

          this.cartAppIds.clear();

          cart.items.forEach(item => {
            this.cartAppIds.add(
              item.applicationId
            );
          });
        },

        error: (err) => {
          console.error(
            'Failed to load cart:',
            err
          );
        }
      });
  }

  addToCart(
    applicationId: string
  ): void {

    if (this.cartLoading[applicationId]) {
      return;
    }

    this.cartLoading[applicationId] = true;

    const payload = {
      applicationId,
      quantity: 1
    };

    this.cartService
      .addToCart(payload)
      .subscribe({
        next: () => {

          this.cartAppIds.add(
            applicationId
          );

          this.cartLoading[applicationId] =
            false;
        },

        error: (err) => {

          console.error(
            'Failed to add item to cart:',
            err
          );

          this.cartLoading[applicationId] =
            false;
        }
      });
  }

  getCategoryName(): string {
    const category = this.categories.find(
      c => `${c.id}` === `${this.selectedCategoryId}`
    );

    return category?.name || 'Category';
  }

  removeFromCart(
    applicationId: string
  ): void {

    if (this.cartLoading[applicationId]) {
      return;
    }

    this.cartLoading[applicationId] = true;

    this.cartService
      .removeItemFromCart(
        applicationId
      )
      .subscribe({
        next: () => {

          this.cartAppIds.delete(
            applicationId
          );

          this.cartLoading[applicationId] =
            false;
        },

        error: (err) => {

          console.error(
            'Failed to remove item from cart:',
            err
          );

          this.cartLoading[applicationId] =
            false;
        }
      });
  }

  ngOnDestroy(): void {
    Object
      .values(this.imageUrls)
      .forEach(url => {
        URL.revokeObjectURL(url);
      });
  }
}