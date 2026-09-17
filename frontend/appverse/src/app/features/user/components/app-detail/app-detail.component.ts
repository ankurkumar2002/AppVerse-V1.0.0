import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { ApplicationResponse } from '../../../application/models/application-response';
import { ApplicationService } from '../../../application/services/application.service';
import { CartService } from '../../../cart/services/cart.service';

interface ScreenshotView {
  id: string;
  filename: string;
  caption: string;
  order: number;
  imageUrl: string;
  loading: boolean;
  error: boolean;
}

@Component({
  selector: 'app-app-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app-detail.component.html',
  styleUrls: ['./app-detail.component.scss']
})
export class AppDetailComponent implements OnInit, OnDestroy {

  application: ApplicationResponse | null = null;

  loading = true;
  error = '';

  thumbnailUrl = '';
  thumbnailLoading = false;
  thumbnailError = false;

  screenshots: ScreenshotView[] = [];

  isInCart = false;
  cartLoading = false;

  imageViewerOpen = false;
  selectedImageUrl = '';
  selectedImageAlt = '';

  private routeSubscription?: Subscription;
  private objectUrls: string[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private applicationService: ApplicationService,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    this.routeSubscription = this.route.paramMap.subscribe(params => {
      const id = params.get('id');

      if (!id) {
        this.loading = false;
        this.error = 'Application ID is missing.';
        return;
      }

      this.loadApplication(id);
    });
  }

  ngOnDestroy(): void {
    this.routeSubscription?.unsubscribe();

    this.objectUrls.forEach(url => {
      URL.revokeObjectURL(url);
    });

    this.objectUrls = [];
  }

  loadApplication(id: string): void {
    this.loading = true;
    this.error = '';
    this.application = null;
    this.screenshots = [];
    this.thumbnailUrl = '';
    this.thumbnailError = false;
    this.thumbnailLoading = false;
    this.isInCart = false;

    this.applicationService.getPublishedApplicationById(id).subscribe({
      next: application => {
        this.application = application;
        this.loading = false;

        this.loadThumbnail();
        this.prepareScreenshots();
        this.checkCartStatus();
      },
      error: error => {
        console.error('Failed to load application:', error);

        this.loading = false;

        this.error =
          error?.error?.message ||
          error?.message ||
          'Unable to load application details.';
      }
    });
  }

  loadThumbnail(): void {
    if (!this.application?.thumbnailUrl) {
      this.thumbnailError = true;
      return;
    }

    this.thumbnailLoading = true;
    this.thumbnailError = false;

    this.applicationService
      .getImageAsBlob('thumbnails', this.application.thumbnailUrl)
      .subscribe({
        next: blob => {
          const url = URL.createObjectURL(blob);

          this.objectUrls.push(url);
          this.thumbnailUrl = url;
          this.thumbnailLoading = false;
        },
        error: error => {
          console.error('Failed to load thumbnail:', error);

          this.thumbnailLoading = false;
          this.thumbnailError = true;
        }
      });
  }

  prepareScreenshots(): void {
    const sourceScreenshots = this.application?.screenshots;

    if (!Array.isArray(sourceScreenshots)) {
      this.screenshots = [];
      return;
    }

    this.screenshots = sourceScreenshots.map((screenshot, index) => ({
      id: String(screenshot?.id ?? index),
      filename: screenshot?.filename ?? '',
      caption:
        screenshot?.caption ||
        screenshot?.filename ||
        `Screenshot ${index + 1}`,
      order: screenshot?.order ?? index,
      imageUrl: '',
      loading: false,
      error: false
    }));

    this.screenshots.forEach((screenshot, index) => {
      this.loadScreenshot(
        screenshot,
        sourceScreenshots[index]
      );
    });
  }

  loadScreenshot(
    view: ScreenshotView,
    source: any
  ): void {
    const imagePath =
      source?.url ||
      source?.imageUrl ||
      source?.filename ||
      '';

    if (!imagePath) {
      view.error = true;
      return;
    }

    view.loading = true;
    view.error = false;

    this.applicationService
      .getImageAsBlob('screenshots', imagePath)
      .subscribe({
        next: blob => {
          const url = URL.createObjectURL(blob);

          this.objectUrls.push(url);
          view.imageUrl = url;
          view.loading = false;
        },
        error: error => {
          console.error(
            `Failed to load screenshot ${view.id}:`,
            error
          );

          view.loading = false;
          view.error = true;
        }
      });
  }

  checkCartStatus(): void {
    if (!this.application?.id) {
      this.isInCart = false;
      return;
    }

    if (this.isFreeApplication()) {
      this.isInCart = false;
      return;
    }

    this.cartService.getCart().subscribe({
      next: cart => {
        const items = Array.isArray(cart?.items)
          ? cart.items
          : [];

        this.isInCart = items.some(
          (item: any) =>
            item?.applicationId === this.application?.id
        );
      },
      error: error => {
        console.error(
          'Failed to check cart status:',
          error
        );

        this.isInCart = false;
      }
    });
  }

  addToCart(): void {
    if (
      !this.application?.id ||
      !this.isPaidApplication() ||
      this.cartLoading
    ) {
      return;
    }

    this.cartLoading = true;

    this.cartService
      .addToCart({
        applicationId: this.application.id,
        quantity: 1
      })
      .subscribe({
        next: () => {
          this.isInCart = true;
          this.cartLoading = false;
        },
        error: error => {
          console.error(
            'Failed to add application to cart:',
            error
          );

          this.cartLoading = false;
        }
      });
  }

  removeFromCart(): void {
    if (
      !this.application?.id ||
      !this.isInCart ||
      this.cartLoading
    ) {
      return;
    }

    this.cartLoading = true;

    this.cartService
      .removeItemFromCart(this.application.id)
      .subscribe({
        next: () => {
          this.isInCart = false;
          this.cartLoading = false;
        },
        error: error => {
          console.error(
            'Failed to remove application from cart:',
            error
          );

          this.cartLoading = false;
        }
      });
  }

  goToCart(): void {
    this.router.navigate(['/user/cart']);
  }

  goBack(): void {
    this.router.navigate(['/user/apps']);
  }

  visitApplication(): void {
    if (
      !this.application ||
      !this.isFreeApplication() ||
      !this.application.accessUrl
    ) {
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
      return;
    }

    window.open(
      this.application.websiteUrl,
      '_blank',
      'noopener,noreferrer'
    );
  }

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

  openImage(
    imageUrl: string,
    alt: string
  ): void {
    if (!imageUrl) {
      return;
    }

    this.selectedImageUrl = imageUrl;
    this.selectedImageAlt = alt;
    this.imageViewerOpen = true;
  }

  closeImageViewer(): void {
    this.imageViewerOpen = false;
    this.selectedImageUrl = '';
    this.selectedImageAlt = '';
  }

  isFreeApplication(): boolean {
    return this.application?.monetizationType === 'FREE';
  }

  isPaidApplication(): boolean {
    const type = this.application?.monetizationType;

    return (
      type === 'PAID' ||
      type === 'SUBSCRIPTION'
    );
  }

  getMonetizationLabel(): string {
    const type = this.application?.monetizationType;

    switch (type) {
      case 'FREE':
        return 'Free';

      case 'PAID':
        return 'Paid';

      case 'SUBSCRIPTION':
        return 'Subscription';

      default:
        return 'Application';
    }
  }

  getPriceLabel(): string {
    if (!this.application) {
      return '';
    }

    if (this.application.monetizationType === 'FREE') {
      return 'Free';
    }

    const price = this.application.price ?? 0;

    if (price === 0) {
      return 'Contact developer';
    }

    return `${this.application.currency || 'INR'} ${price}`;
  }

  getRatingStars(): number[] {
    return [1, 2, 3, 4, 5];
  }

  isFilledStar(star: number): boolean {
    const rating =
      this.application?.averageRating ?? 0;

    return star <= Math.round(rating);
  }

  getRating(): string {
    const rating =
      this.application?.averageRating ?? 0;

    return rating > 0
      ? rating.toFixed(1)
      : 'No rating';
  }

  getRatingCount(): number {
    return this.application?.ratingCount ?? 0;
  }

  getDeveloperName(): string {
    return (
      this.application?.developerName ||
      'Unknown developer'
    );
  }

  getCategoryName(): string {
    return (
      this.application?.categoryName ||
      'Uncategorized'
    );
  }

  getVersion(): string {
    return (
      this.application?.version ||
      'N/A'
    );
  }
}