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
export class AppListComponent
  implements OnInit, OnDestroy {


  // ============================================================
  // MATERIAL TABLE
  // ============================================================

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

  dataSource =
    new MatTableDataSource<ApplicationResponse>();

  isLoading = false;


  @ViewChild(MatPaginator)
  paginator!: MatPaginator;

  @ViewChild(MatSort)
  sort!: MatSort;


  // ============================================================
  // APPLICATION DATA
  // ============================================================

  /**
   * Original applications received from backend
   * for the CURRENT backend page.
   *
   * Never modify this array directly when filtering.
   */
  allApplications: ApplicationResponse[] = [];


  // ============================================================
  // CATEGORIES
  // ============================================================

  categories: Category[] = [];

  /**
   * Selected category ID.
   *
   * Empty string = all categories.
   */
  selectedCategoryId = '';


  // ============================================================
  // SEARCH
  // ============================================================

  searchTerm = '';


  // ============================================================
  // SORT
  // ============================================================

  sortOption = 'default';


  // ============================================================
  // CART
  // ============================================================

  cartAppIds = new Set<string>();


  // ============================================================
  // PAGINATION
  // ============================================================

  currentPage = 0;

  pageSize = 6;

  totalPages = 0;

  pages: number[] = [];


  // ============================================================
  // IMAGES
  // ============================================================

  imageUrls: Record<string, string> = {};


  // ============================================================
  // CONSTRUCTOR
  // ============================================================

  constructor(
    private appService: ApplicationService,
    private cartService: CartService,
    private categoryService: CategoryService
  ) {}


  // ============================================================
  // INIT
  // ============================================================

  ngOnInit(): void {

    this.loadApplications();

    this.loadCategories();

    this.loadCart();

  }


  // ============================================================
  // LOAD APPLICATIONS
  // ============================================================

  loadApplications(): void {

    this.isLoading = true;

    this.appService
      .getPublushedApplications(
        this.currentPage,
        this.pageSize
      )
      .subscribe({

        next: (data) => {

          console.log(
            'Applications:',
            data
          );


          // ------------------------------------------------------
          // IMPORTANT
          // Keep original backend response.
          // ------------------------------------------------------

          this.allApplications = [
            ...data.content
          ];


          // ------------------------------------------------------
          // Backend pagination
          // ------------------------------------------------------

          this.totalPages =
            data.totalPages;

          this.pages =
            Array.from(
              { length: this.totalPages },
              (_, index) => index
            );


          // ------------------------------------------------------
          // Apply search/category/sort
          // ------------------------------------------------------

          this.applyFilters();


          // ------------------------------------------------------
          // Load thumbnails
          // ------------------------------------------------------

          data.content.forEach(
            app => this.loadThumbnail(app)
          );


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


  // ============================================================
  // LOAD CATEGORIES
  // ============================================================

  loadCategories(): void {

    this.categoryService
      .getAll()
      .subscribe({

        next: (categories) => {

          console.log(
            'Categories:',
            categories
          );

          this.categories =
            categories;

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


  // ============================================================
  // CATEGORY CHANGE
  // ============================================================

  onCategoryChange(
    event: Event
  ): void {

    const select =
      event.target as HTMLSelectElement;

    const categoryId =
      select.value;


    console.log(
      'Category changed:',
      categoryId
    );


    this.selectedCategoryId =
      categoryId || '';


    this.applyFilters();

  }


  // ============================================================
  // SEARCH
  // ============================================================

  onSearch(
    event: Event
  ): void {

    const input =
      event.target as HTMLInputElement;

    this.searchTerm =
      input.value
        .trim()
        .toLowerCase();


    this.applyFilters();

  }


  // ============================================================
  // SORT CHANGE
  // ============================================================

  onSortChange(
    event: Event
  ): void {

    const select =
      event.target as HTMLSelectElement;

    this.sortOption =
      select.value || 'default';


    this.applyFilters();

  }


  // ============================================================
  // APPLY FILTERS
  // ============================================================

  applyFilters(): void {

    /*
     * VERY IMPORTANT:
     *
     * Every time a filter changes we start from
     * allApplications.
     *
     * We NEVER filter dataSource.data.
     *
     * This prevents:
     *
     * Category A
     *      ↓
     * filtered list
     *      ↓
     * Category B
     *
     * from becoming broken.
     */

    let result =
      [...this.allApplications];


    // ==========================================================
    // CATEGORY
    // ==========================================================

    if (this.selectedCategoryId) {

      result =
        result.filter(app => {

          return String(app.categoryId) ===
                 String(this.selectedCategoryId);

        });

    }


    // ==========================================================
    // SEARCH
    // ==========================================================

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


    // ==========================================================
    // SORT
    // ==========================================================

    switch (this.sortOption) {

      // --------------------------------------------------------
      // NAME A-Z
      // --------------------------------------------------------

      case 'name-asc':

        result.sort(
          (a, b) =>
            (a.name || '').localeCompare(
              b.name || ''
            )
        );

        break;


      // --------------------------------------------------------
      // NAME Z-A
      // --------------------------------------------------------

      case 'name-desc':

        result.sort(
          (a, b) =>
            (b.name || '').localeCompare(
              a.name || ''
            )
        );

        break;


      // --------------------------------------------------------
      // RATING HIGH -> LOW
      // --------------------------------------------------------

      case 'rating-high':

        result.sort(
          (a, b) =>
            (b.averageRating || 0) -
            (a.averageRating || 0)
        );

        break;


      // --------------------------------------------------------
      // RATING LOW -> HIGH
      // --------------------------------------------------------

      case 'rating-low':

        result.sort(
          (a, b) =>
            (a.averageRating || 0) -
            (b.averageRating || 0)
        );

        break;


      // --------------------------------------------------------
      // PRICE LOW -> HIGH
      // --------------------------------------------------------

      case 'price-low':

        result.sort(
          (a, b) =>
            (a.price || 0) -
            (b.price || 0)
        );

        break;


      // --------------------------------------------------------
      // PRICE HIGH -> LOW
      // --------------------------------------------------------

      case 'price-high':

        result.sort(
          (a, b) =>
            (b.price || 0) -
            (a.price || 0)
        );

        break;


      // --------------------------------------------------------
      // DEFAULT
      // --------------------------------------------------------

      case 'default':
      default:

        break;

    }


    // ==========================================================
    // UPDATE UI
    // ==========================================================

    this.dataSource.data =
      result;


    // ==========================================================
    // MATERIAL COMPONENTS
    // ==========================================================

    if (this.paginator) {

      this.dataSource.paginator =
        this.paginator;

    }

    if (this.sort) {

      this.dataSource.sort =
        this.sort;

    }

  }


  // ============================================================
  // RESET FILTERS
  // ============================================================

  resetFilters(): void {

    this.searchTerm = '';

    this.selectedCategoryId = '';

    this.sortOption = 'default';


    this.applyFilters();

  }


  // ============================================================
  // PAGINATION
  // ============================================================

  goToPage(
    page: number
  ): void {

    if (
      page < 0 ||
      page >= this.totalPages
    ) {

      return;

    }


    this.currentPage =
      page;


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

    if (
      this.currentPage > 0
    ) {

      this.currentPage--;

      this.loadApplications();

    }

  }


  // ============================================================
  // THUMBNAIL
  // ============================================================

  getThumbnailUrl(
    path?: string
  ): string {

    if (!path) {

      return 'assets/default-app.png';

    }


    const filename =
      path
        .split(/[/\\]/)
        .pop();


    if (!filename) {

      return 'assets/default-app.png';

    }


    return (
      `http://localhost:9000/api/apps/images/thumbnails/${filename}`
    );

  }


  // ============================================================
  // LOAD THUMBNAIL
  // ============================================================

  loadThumbnail(
    app: ApplicationResponse
  ): void {

    if (!app.thumbnailUrl) {

      return;

    }


    const filename =
      app.thumbnailUrl
        .split(/[/\\]/)
        .pop();


    if (!filename) {

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

        },

        error: (err) => {

          console.error(
            'Failed to load thumbnail:',
            err
          );

        }

      });

  }


  // ============================================================
  // CART
  // ============================================================

  loadCart(): void {

    this.cartService
      .getCart()
      .subscribe({

        next: (cart) => {

          this.cartAppIds.clear();


          cart.items.forEach(
            item => {

              this.cartAppIds.add(
                item.applicationId
              );

            }
          );

        },

        error: (err) => {

          console.error(
            'Failed to load cart:',
            err
          );

        }

      });

  }


  // ============================================================
  // ADD TO CART
  // ============================================================

  addToCart(
    applicationId: string
  ): void {

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

        },

        error: (err) => {

          console.error(
            'Failed to add item to cart:',
            err
          );

        }

      });

  }


  // ============================================================
  // REMOVE FROM CART
  // ============================================================

  removeFromCart(
    applicationId: string
  ): void {

    this.cartService
      .removeItemFromCart(
        applicationId
      )
      .subscribe({

        next: () => {

          this.cartAppIds.delete(
            applicationId
          );

        },

        error: (err) => {

          console.error(
            'Failed to remove item from cart:',
            err
          );

        }

      });

  }


  // ============================================================
  // DESTROY
  // ============================================================

  ngOnDestroy(): void {

    Object
      .values(this.imageUrls)
      .forEach(url => {

        URL.revokeObjectURL(url);

      });

  }

}