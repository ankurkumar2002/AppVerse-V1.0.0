import {
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';

import {
  CommonModule,
  DecimalPipe
} from '@angular/common';

import {
  HttpClient
} from '@angular/common/http';

import {
  Router,
  RouterModule
} from '@angular/router';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  MatSnackBar,
  MatSnackBarModule
} from '@angular/material/snack-bar';

import {
  forkJoin,
  Observable
} from 'rxjs';

import {
  ApplicationService
} from '../../../application/services/application.service';

import {
  ApplicationResponse
} from '../../../application/models/application-response';

import {
  CartService
} from '../../../cart/services/cart.service';

interface CartItem {
  applicationId: string;
  quantity?: number;
}

interface CartResponse {
  id?: string;
  userId?: string;
  items: CartItem[];
}

interface CartDisplayItem {
  application: ApplicationResponse;
  unitPrice: number;
  imageUrl: string | null;
  imageLoading: boolean;
  imageFailed: boolean;
}

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatSnackBarModule,
    DecimalPipe
  ],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})
export class CartComponent implements OnInit, OnDestroy {

  private readonly baseUrl =
    'http://localhost:9000/api/v1/carts';

  cartItems: CartDisplayItem[] = [];

  isLoading = true;

  isClearing = false;

  itemLoading: Record<string, boolean> = {};

  cartError = false;

  readonly taxRate = 0.10;

  constructor(
    private cartService: CartService,
    private applicationService: ApplicationService,
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.isLoading = true;
    this.cartError = false;

    this.cartService
      .getCart()
      .subscribe({
        next: (cart: CartResponse) => {

          if (
            !cart ||
            !cart.items ||
            cart.items.length === 0
          ) {
            this.cartItems = [];
            this.isLoading = false;
            return;
          }

          this.loadApplications(cart.items);
        },

        error: (error: any) => {
          console.error(
            'Failed to load cart:',
            error
          );

          this.cartItems = [];
          this.cartError = true;
          this.isLoading = false;

          this.showMessage(
            'Unable to load your cart'
          );
        }
      });
  }

  private loadApplications(
    items: CartItem[]
  ): void {

    const requests: Observable<ApplicationResponse>[] =
      items.map(item =>
        this.applicationService
          .getPublishedApplicationById(
            item.applicationId
          )
      );

    forkJoin(requests)
      .subscribe({
        next: (
          applications: ApplicationResponse[]
        ) => {

          this.cartItems =
            applications.map(application => ({
              application,
              unitPrice: this.getUnitPrice(application),
              imageUrl: null,
              imageLoading: false,
              imageFailed: false
            }));

          this.cartItems.forEach(item =>
            this.loadThumbnail(item)
          );

          this.isLoading = false;
        },

        error: (error) => {
          console.error(
            'Failed to load cart applications:',
            error
          );

          this.cartItems = [];
          this.cartError = true;
          this.isLoading = false;

          this.showMessage(
            'Some applications in your cart could not be loaded'
          );
        }
      });
  }

  private getUnitPrice(
    application: ApplicationResponse
  ): number {

    if (
      application.monetizationType === 'FREE' ||
      application.isFree
    ) {
      return 0;
    }

    return Number(
      application.price || 0
    );
  }

  loadThumbnail(
    item: CartDisplayItem
  ): void {

    const application =
      item.application;

    if (!application.thumbnailUrl) {
      item.imageFailed = true;
      return;
    }

    item.imageLoading = true;
    item.imageFailed = false;

    this.applicationService
      .getImageAsBlob(
        'thumbnails',
        application.thumbnailUrl
      )
      .subscribe({
        next: (blob: Blob) => {

          if (item.imageUrl) {
            URL.revokeObjectURL(
              item.imageUrl
            );
          }

          item.imageUrl =
            URL.createObjectURL(blob);

          item.imageLoading = false;
        },

        error: (error) => {

          console.error(
            'Failed to load cart thumbnail:',
            error
          );

          item.imageLoading = false;
          item.imageFailed = true;
        }
      });
  }

  removeItem(
    item: CartDisplayItem
  ): void {

    const applicationId =
      item.application.id;

    if (
      this.itemLoading[applicationId]
    ) {
      return;
    }

    this.itemLoading[applicationId] = true;

    this.cartService
      .removeItemFromCart(
        applicationId
      )
      .subscribe({
        next: () => {

          this.cartItems =
            this.cartItems.filter(
              current =>
                current.application.id !==
                applicationId
            );

          this.itemLoading[applicationId] = false;

          this.showMessage(
            'Application removed from cart'
          );
        },

        error: (error: any) => {

          console.error(
            'Failed to remove cart item:',
            error
          );

          this.itemLoading[applicationId] = false;

          this.showMessage(
            'Unable to remove application'
          );
        }
      });
  }

  clearCart(): void {

    if (
      this.isClearing ||
      this.cartItems.length === 0
    ) {
      return;
    }

    const confirmed =
      window.confirm(
        'Are you sure you want to remove all applications from your cart?'
      );

    if (!confirmed) {
      return;
    }

    this.isClearing = true;

    this.http
      .delete(
        `${this.baseUrl}/mine`
      )
      .subscribe({
        next: () => {

          this.cartItems = [];
          this.isClearing = false;

          this.showMessage(
            'Cart cleared successfully'
          );
        },

        error: (error) => {

          console.error(
            'Failed to clear cart:',
            error
          );

          this.isClearing = false;

          this.showMessage(
            'Unable to clear cart'
          );
        }
      });
  }

  continueShopping(): void {
    this.router.navigate([
      '/user/apps'
    ]);
  }

  viewApplication(
    applicationId: string
  ): void {

    this.router.navigate([
      '/user/apps',
      applicationId
    ]);
  }

  checkout(): void {

    if (
      this.cartItems.length === 0
    ) {
      return;
    }

    this.showMessage(
      'Checkout will be available here next'
    );
  }

  get itemCount(): number {
    return this.cartItems.length;
  }

  get subtotal(): number {
    return this.cartItems.reduce(
      (total, item) =>
        total + item.unitPrice,
      0
    );
  }

  get tax(): number {
    return this.subtotal * this.taxRate;
  }

  get total(): number {
    return this.subtotal + this.tax;
  }

  get currency(): string {

    const pricedItem =
      this.cartItems.find(
        item =>
          !item.application.isFree
      );

    return (
      pricedItem?.application.currency ||
      'INR'
    );
  }

  formatPrice(
    amount: number
  ): string {

    return new Intl.NumberFormat(
      'en-IN',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    ).format(amount);
  }

  isFree(
    item: CartDisplayItem
  ): boolean {

    return (
      item.application.monetizationType === 'FREE' ||
      item.application.isFree
    );
  }

  getPriceLabel(
    item: CartDisplayItem
  ): string {

    if (
      item.application.monetizationType ===
      'SUBSCRIPTION'
    ) {
      return 'Subscription';
    }

    if (this.isFree(item)) {
      return 'Free';
    }

    return `${item.application.currency || 'INR'} ${this.formatPrice(item.unitPrice)}`;
  }

  getItemType(
    item: CartDisplayItem
  ): string {

    if (
      item.application.monetizationType ===
      'SUBSCRIPTION'
    ) {
      return 'SUBSCRIPTION';
    }

    if (this.isFree(item)) {
      return 'FREE';
    }

    return 'PURCHASE';
  }

  trackByApplicationId(
    index: number,
    item: CartDisplayItem
  ): string {

    return item.application.id;
  }

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

  ngOnDestroy(): void {

    this.cartItems.forEach(item => {

      if (item.imageUrl) {
        URL.revokeObjectURL(
          item.imageUrl
        );
      }
    });
  }
}