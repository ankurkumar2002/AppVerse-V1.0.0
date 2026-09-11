import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import {
  ActivatedRoute,
  Router,
  RouterModule
} from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import {
  MatSnackBar,
  MatSnackBarModule
} from '@angular/material/snack-bar';

import { ApplicationService } from '../../../application/services/application.service';
import { ApplicationResponse } from '../../../application/models/application-response';
import {
  Screenshot
} from '../../../application/models/application-detail';
import { CartService } from '../../../cart/services/cart.service';

@Component({
  selector: 'app-app-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './app-detail.component.html',
  styleUrl: './app-detail.component.scss'
})
export class AppDetailComponent implements OnInit, OnDestroy {

  application: ApplicationResponse | null = null;

  isLoading = true;

  imageLoading = true;
  imageLoadFailed = false;

  cartLoading = false;
  isInCart = false;

  thumbnailUrl: string | null = null;

  /*
   * Screenshot blob URLs
   * Key = screenshot._id
   */
  screenshotUrls: {
    [key: string]: string
  } = {};

  screenshotLoading: {
    [key: string]: boolean
  } = {};

  screenshotFailed: {
    [key: string]: boolean
  } = {};

  /*
   * Demo rating
   */
  readonly demoRating = 4.5;

  /*
   * Image lightbox
   */
  isImageViewerOpen = false;

  selectedImageUrl: string | null = null;

  selectedImageAlt = '';

  private applicationId: string | null = null;


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private applicationService: ApplicationService,
    private cartService: CartService,
    private snackBar: MatSnackBar
  ) { }


  ngOnInit(): void {

    this.applicationId =
      this.route.snapshot.paramMap.get('id');

    if (!this.applicationId) {

      this.showMessage(
        'Application ID is missing'
      );

      this.router.navigate([
        '/user/apps'
      ]);

      return;
    }

    this.loadApplication(
      this.applicationId
    );

    this.loadCart();
  }


  /*
   * ==========================================
   * APPLICATION
   * ==========================================
   */

  loadApplication(id: string): void {

    this.isLoading = true;

    this.applicationService
      .getPublishedApplicationById(id)
      .subscribe({

        next: (response: ApplicationResponse) => {

          this.application = response;

          this.isLoading = false;

          this.loadThumbnail(response);

          this.loadScreenshots(response);
        },

        error: (error) => {

          console.error(
            'Error loading application:',
            error
          );

          this.isLoading = false;

          this.showMessage(
            'Application not found or no longer published'
          );

          this.router.navigate([
            '/user/apps'
          ]);
        }
      });
  }


  /*
   * ==========================================
   * THUMBNAIL
   * ==========================================
   */

  loadThumbnail(
    application: ApplicationResponse
  ): void {

    this.imageLoading = true;
    this.imageLoadFailed = false;

    if (!application.thumbnailUrl) {

      this.imageLoading = false;
      this.imageLoadFailed = true;

      return;
    }

    const filename =
      this.extractFileName(
        application.thumbnailUrl
      );

    if (!filename) {

      this.imageLoading = false;
      this.imageLoadFailed = true;

      return;
    }

    this.applicationService
      .getImageAsBlob(
        'thumbnails',
        filename
      )
      .subscribe({

        next: (blob: Blob) => {

          if (this.thumbnailUrl) {

            URL.revokeObjectURL(
              this.thumbnailUrl
            );
          }

          this.thumbnailUrl =
            URL.createObjectURL(blob);

          this.imageLoading = false;
          this.imageLoadFailed = false;
        },

        error: (error) => {

          console.error(
            `Error loading thumbnail '${filename}':`,
            error
          );

          this.imageLoading = false;
          this.imageLoadFailed = true;
        }
      });
  }


  /*
   * ==========================================
   * SCREENSHOTS
   * ==========================================
   */

  loadScreenshots(application: any): void {

    this.screenshotUrls = {};
    this.screenshotLoading = {};
    this.screenshotFailed = {};

    if (
      !application?.screenshots ||
      application.screenshots.length === 0
    ) {
      return;
    }

    application.screenshots.forEach(
      (screenshot: any, index: number) => {

        const imagePath =
          screenshot?.imageUrl ||
          screenshot?.url;

        if (!imagePath) {

          this.screenshotFailed[index] = true;
          this.screenshotLoading[index] = false;

          return;
        }

        const filename =
          this.extractFileName(imagePath);

        if (!filename) {

          this.screenshotFailed[index] = true;
          this.screenshotLoading[index] = false;

          return;
        }

        this.screenshotLoading[index] = true;
        this.screenshotFailed[index] = false;

        this.applicationService
          .getImageAsBlob(
            'screenshots',
            filename
          )
          .subscribe({

            next: (blob: Blob) => {

              if (this.screenshotUrls[index]) {
                URL.revokeObjectURL(
                  this.screenshotUrls[index]
                );
              }

              this.screenshotUrls[index] =
                URL.createObjectURL(blob);

              this.screenshotLoading[index] = false;
              this.screenshotFailed[index] = false;
            },

            error: (error) => {

              console.error(
                `Failed to load screenshot ${index}:`,
                error
              );

              this.screenshotLoading[index] = false;
              this.screenshotFailed[index] = true;
            }
          });
      }
    );
  }


  /*
   * ==========================================
   * FILE NAME
   * ==========================================
   */

  extractFileName(
    fullPath: string
  ): string {

    if (!fullPath) {
      return '';
    }

    return (
      fullPath
        .split('/')
        .pop()
        ?.split('?')[0]
        .split('#')[0] || ''
    );
  }


  /*
   * ==========================================
   * IMAGE VIEWER
   * ==========================================
   */

  openImageViewer(
    imageUrl: string,
    altText: string
  ): void {

    this.selectedImageUrl =
      imageUrl;

    this.selectedImageAlt =
      altText;

    this.isImageViewerOpen = true;

    document.body.style.overflow =
      'hidden';
  }


  closeImageViewer(): void {

    this.isImageViewerOpen = false;

    this.selectedImageUrl = null;

    this.selectedImageAlt = '';

    document.body.style.overflow =
      '';
  }


  onImageViewerKeydown(
    event: KeyboardEvent
  ): void {

    if (
      event.key === 'Escape' &&
      this.isImageViewerOpen
    ) {

      this.closeImageViewer();
    }
  }


  /*
   * ==========================================
   * CART
   * ==========================================
   */

  loadCart(): void {

    this.cartService
      .getCart()
      .subscribe({

        next: (cart) => {

          if (!cart?.items) {

            this.isInCart = false;

            return;
          }

          this.isInCart =
            cart.items.some(
              (item: any) =>
                item.applicationId ===
                this.applicationId
            );
        },

        error: () => {

          this.isInCart = false;
        }
      });
  }


  addToCart(): void {

    if (
      !this.applicationId ||
      this.cartLoading
    ) {
      return;
    }

    this.cartLoading = true;

    this.cartService
      .addToCart({
        applicationId: this.applicationId,
        quantity: 1
      })
      .subscribe({

        next: () => {

          this.isInCart = true;

          this.cartLoading = false;

          this.showMessage(
            'Application added to cart'
          );
        },

        error: (error) => {

          this.cartLoading = false;

          if (error?.status === 409) {

            this.isInCart = true;

            this.showMessage(
              'Application is already in your cart'
            );

          } else {

            this.showMessage(
              'Unable to add application to cart'
            );
          }
        }
      });
  }


  removeFromCart(): void {

    if (
      !this.applicationId ||
      this.cartLoading
    ) {
      return;
    }

    this.cartLoading = true;

    this.cartService
      .removeItemFromCart(
        this.applicationId
      )
      .subscribe({

        next: () => {

          this.isInCart = false;

          this.cartLoading = false;

          this.showMessage(
            'Application removed from cart'
          );
        },

        error: () => {

          this.cartLoading = false;

          this.showMessage(
            'Unable to remove application from cart'
          );
        }
      });
  }


  /*
   * ==========================================
   * EXTERNAL LINKS
   * ==========================================
   */

  visitSupport(): void {

    if (!this.application?.supportUrl) {
      return;
    }

    window.open(
      this.application.supportUrl,
      '_blank',
      'noopener,noreferrer'
    );
  }


  visitApplication(): void {

    if (!this.application?.accessUrl) {

      this.showMessage(
        'Application access URL is not available'
      );

      return;
    }

    window.open(
      this.application.accessUrl,
      '_blank',
      'noopener,noreferrer'
    );
  }


  visitWebsite(): void {

    if (!this.application?.websiteUrl) {

      this.showMessage(
        'Application website is not available'
      );

      return;
    }

    window.open(
      this.application.websiteUrl,
      '_blank',
      'noopener,noreferrer'
    );
  }


  /*
   * ==========================================
   * NAVIGATION
   * ==========================================
   */

  goBack(): void {

    this.router.navigate([
      '/user/apps'
    ]);
  }


  /*
   * ==========================================
   * MESSAGES
   * ==========================================
   */

  showMessage(
    message: string
  ): void {

    this.snackBar.open(
      message,
      'Close',
      {
        duration: 3000,
        horizontalPosition: 'right',
        verticalPosition: 'bottom'
      }
    );
  }


  /*
   * ==========================================
   * CLEANUP
   * ==========================================
   */

  ngOnDestroy(): void {

    if (this.thumbnailUrl) {

      URL.revokeObjectURL(
        this.thumbnailUrl
      );
    }

    Object.values(
      this.screenshotUrls
    ).forEach(
      (url: string) => {

        URL.revokeObjectURL(url);
      }
    );

    document.body.style.overflow =
      '';
  }
}