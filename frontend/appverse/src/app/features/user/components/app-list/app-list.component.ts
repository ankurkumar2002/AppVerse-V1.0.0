import { Component, ViewChild } from '@angular/core';
import { UserAuthService } from '../../services/user/user-auth.service';
import { ApplicationDetail } from '../../../application/models/application-detail';
import { ApplicationService } from '../../../application/services/application.service';
import { MatTableDataSource } from '@angular/material/table';
import { ApplicationResponse } from '../../../application/models/application-response';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatIcon } from "@angular/material/icon";
import { Router, RouterModule } from '@angular/router';
import { CartService } from '../../../cart/services/cart.service';

@Component({
  selector: 'app-app-list',
  imports: [MatIcon, RouterModule],
  templateUrl: './app-list.component.html',
  styleUrl: './app-list.component.scss'
})
export class AppListComponent {

  displayedColumns = ['name', 'description', 'version', 'categoryId', 'currency', 'price', 'platforms', 'accessUrl', 'websiteUrl']
  dataSource = new MatTableDataSource<ApplicationResponse>();
  isLoading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  cartAppIds = new Set<string>();

  currentPage = 0;
  pageSize = 6;

  totalPages = 0;

  pages: number[] = [];
  imageUrls: Record<string, string> = {};

  constructor(private appService: ApplicationService, private router: Router, private cartService: CartService) { }

  ngOnInit(): void {
    this.loadApplications();
    this.loadCart();

  }

  loadApplications() {
    this.isLoading = true;
    this.appService.getPublushedApplications(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        console.log(data)
        this.dataSource.data = data.content;

        this.totalPages = data.totalPages;

        this.pages = Array(this.totalPages).fill(0).map((_, index) => index);
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.isLoading = false;
        data.content.forEach(app => {
          this.loadThumnail(app);
        })
      },
      error: (err) => {
        console.error('Failed to load applications', err);
        this.isLoading = false;
      }
    })
  }

  goToPage(page: number) {
    this.currentPage = page;
    this.loadApplications();
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;

      this.loadApplications();
    }
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;

      this.loadApplications();
    }
  }

  getThumbnailUrl(path?: string): string {

    if (!path) {
      return 'assets/default-app.png';
    }

    const filename = path.split(/[/\\]/).pop();

    return `http://localhost:9000/api/apps/images/thumbnails/${filename}`;
  }

  loadThumnail(app: ApplicationResponse) {

    if (!app.thumbnailUrl) {
      return;
    }

    const filename =
      app.thumbnailUrl
        .split(/[/\\]/)
        .pop();

    this.appService
      .getImageAsBlob(
        'thumbnails',
        filename!
      )
      .subscribe({

        next: (blob) => {

          const objectUrl =
            URL.createObjectURL(blob);

          this.imageUrls[app.id] = objectUrl;
        },

        error: (err) => {
          console.error(
            'Failed to load image.',
            err
          );
        }
      });
  }
  ngOnDestroy(): void {
    Object.values(this.imageUrls).forEach(url => {
      URL.revokeObjectURL(url);
    });
  }

  loadCart(): void {
    this.cartService.getCart().subscribe({
      next: cart => {

        this.cartAppIds.clear();

        cart.items.forEach(item => {
          this.cartAppIds.add(item.applicationId);
        });

        console.log(this.cartAppIds);
      },
      error: err => console.error(err)
    });
  }

  addToCart(applicationId: string): void {

    const payload = {
      applicationId,
      quantity: 1
    };

    this.cartService.addToCart(payload).subscribe({

      next: () => {

        this.cartAppIds.add(applicationId);

        console.log('Added to cart successfully');

      },

      error: (err) => {

        console.error('Failed to add item to cart', err);

      }

    });
  }

  removeFromCart(applicationId: string): void {

    this.cartService.removeItemFromCart(applicationId).subscribe({

      next: () => {

        this.cartAppIds.delete(applicationId);

      },

      error: err => console.error(err)

    });
  }

}
