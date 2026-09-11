import { CommonModule } from '@angular/common';
import {
  Component,
  HostListener,
  OnDestroy,
  OnInit
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {
  MatSnackBar,
  MatSnackBarModule
} from '@angular/material/snack-bar';

import { ApplicationService } from '../../services/application.service';
import {
  ApplicationDetail,
  Screenshot
} from '../../models/application-detail';

@Component({
  selector: 'app-application-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './application-detail.component.html',
  styleUrls: ['./application-detail.component.scss']
})
export class ApplicationDetailComponent
  implements OnInit, OnDestroy {

  appId!: string;

  application: ApplicationDetail | null = null;

  thumbnailBlobUrl: string | null = null;

  screenshotBlobUrls: {
    [key: string]: string
  } = {};

  screenshotLoading: {
    [key: string]: boolean
  } = {};

  screenshotFailed: {
    [key: string]: boolean
  } = {};

  isLoading = true;

  isDeleting = false;

  /*
   * Screenshot / image viewer
   */
  isImageViewerOpen = false;

  selectedImageUrl: string | null = null;

  selectedImageAlt = '';

  /*
   * Confirmation dialog
   */
  isDeleteDialogOpen = false;

  /*
   * =========================================================
   * INIT
   * =========================================================
   */

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private applicationService: ApplicationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {

    this.appId =
      this.route.snapshot.paramMap.get('id') || '';

    if (!this.appId) {

      this.showMessage(
        'Application ID is missing'
      );

      this.router.navigate([
        '/developer/applications'
      ]);

      return;
    }

    this.loadApplicationDetails();
  }

  /*
   * =========================================================
   * LOAD APPLICATION
   * =========================================================
   */

  loadApplicationDetails(): void {

    this.isLoading = true;

    this.applicationService
      .getApplicationById(this.appId)
      .subscribe({

        next: (app: ApplicationDetail) => {

          this.application = app;

          this.isLoading = false;

          this.loadThumbnail(app);

          this.loadScreenshots(app);
        },

        error: (error) => {

          console.error(
            'Error loading application:',
            error
          );

          this.isLoading = false;

          this.showMessage(
            'Unable to load application details'
          );

          this.router.navigate([
            '/developer/applications'
          ]);
        }
      });
  }

  /*
   * =========================================================
   * THUMBNAIL
   * =========================================================
   */

  loadThumbnail(
    application: ApplicationDetail
  ): void {

    if (!application.thumbnailUrl) {
      return;
    }

    const filename =
      this.extractFileName(
        application.thumbnailUrl
      );

    if (!filename) {
      return;
    }

    this.applicationService
      .getImageAsBlob(
        'thumbnails',
        filename
      )
      .subscribe({

        next: (blob: Blob) => {

          if (this.thumbnailBlobUrl) {

            URL.revokeObjectURL(
              this.thumbnailBlobUrl
            );
          }

          this.thumbnailBlobUrl =
            URL.createObjectURL(blob);
        },

        error: (error) => {

          console.error(
            `Error fetching thumbnail '${filename}':`,
            error
          );
        }
      });
  }

  /*
   * =========================================================
   * SCREENSHOTS
   * =========================================================
   */

  loadScreenshots(
    application: ApplicationDetail
  ): void {

    if (
      !application.screenshots ||
      application.screenshots.length === 0
    ) {
      return;
    }

    application.screenshots.forEach(
      (screenshot: Screenshot) => {

        const screenshotId =
          screenshot._id;

        if (!screenshot.imageUrl) {

          this.screenshotFailed[
            screenshotId
          ] = true;

          return;
        }

        const filename =
          this.extractFileName(
            screenshot.imageUrl
          );

        if (!filename) {

          this.screenshotFailed[
            screenshotId
          ] = true;

          return;
        }

        this.screenshotLoading[
          screenshotId
        ] = true;

        this.screenshotFailed[
          screenshotId
        ] = false;

        this.applicationService
          .getImageAsBlob(
            'screenshots',
            filename
          )
          .subscribe({

            next: (blob: Blob) => {

              if (
                this.screenshotBlobUrls[
                  screenshotId
                ]
              ) {

                URL.revokeObjectURL(
                  this.screenshotBlobUrls[
                    screenshotId
                  ]
                );
              }

              this.screenshotBlobUrls[
                screenshotId
              ] = URL.createObjectURL(blob);

              this.screenshotLoading[
                screenshotId
              ] = false;

              this.screenshotFailed[
                screenshotId
              ] = false;
            },

            error: (error) => {

              console.error(
                `Error loading screenshot '${filename}':`,
                error
              );

              this.screenshotLoading[
                screenshotId
              ] = false;

              this.screenshotFailed[
                screenshotId
              ] = true;
            }
          });
      }
    );
  }

  /*
   * =========================================================
   * FILE NAME
   * =========================================================
   */

  extractFileName(
    fullPath: string
  ): string {

    if (!fullPath) {
      return '';
    }

    return fullPath
      .split('/')
      .pop()
      ?.split('?')[0]
      .split('#')[0] || '';
  }

  /*
   * =========================================================
   * IMAGE VIEWER
   * =========================================================
   */

  openImageViewer(
    imageUrl: string,
    altText: string
  ): void {

    this.selectedImageUrl =
      imageUrl;

    this.selectedImageAlt =
      altText;

    this.isImageViewerOpen =
      true;

    document.body.style.overflow =
      'hidden';
  }

  closeImageViewer(): void {

    this.isImageViewerOpen =
      false;

    this.selectedImageUrl =
      null;

    this.selectedImageAlt =
      '';

    document.body.style.overflow =
      '';
  }

  @HostListener(
    'document:keydown.escape'
  )
  handleEscapeKey(): void {

    if (this.isImageViewerOpen) {
      this.closeImageViewer();
    }

    if (this.isDeleteDialogOpen) {
      this.closeDeleteDialog();
    }
  }

  /*
   * =========================================================
   * EDIT
   * =========================================================
   */

  editApplication(): void {

    if (!this.appId) {
      return;
    }

    this.router.navigate([
      '/developer/applications',
      'edit',
      this.appId
    ]);
  }

  /*
   * =========================================================
   * DELETE CONFIRMATION
   * =========================================================
   */

  openDeleteDialog(): void {

    if (this.isDeleting) {
      return;
    }

    this.isDeleteDialogOpen = true;

    document.body.style.overflow =
      'hidden';
  }

  closeDeleteDialog(): void {

    if (this.isDeleting) {
      return;
    }

    this.isDeleteDialogOpen = false;

    if (!this.isImageViewerOpen) {
      document.body.style.overflow =
        '';
    }
  }

  /*
   * =========================================================
   * DELETE APPLICATION
   * =========================================================
   */

  deleteApplication(): void {

    if (
      !this.appId ||
      this.isDeleting
    ) {
      return;
    }

    this.isDeleting = true;

    this.applicationService
      .deleteApplication(this.appId)
      .subscribe({

        next: () => {

          this.isDeleting = false;

          this.isDeleteDialogOpen =
            false;

          document.body.style.overflow =
            '';

          this.showMessage(
            'Application deleted successfully'
          );

          this.router.navigate([
            '/developer/applications'
          ]);
        },

        error: (error) => {

          console.error(
            'Error deleting application:',
            error
          );

          this.isDeleting = false;

          this.showMessage(
            'Failed to delete application'
          );
        }
      });
  }

  /*
   * =========================================================
   * WEBSITE
   * =========================================================
   */

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

  /*
   * =========================================================
   * BACK
   * =========================================================
   */

  goBack(): void {

    this.router.navigate([
      '/developer/applications'
    ]);
  }

  /*
   * =========================================================
   * MESSAGE
   * =========================================================
   */

  showMessage(
    message: string
  ): void {

    this.snackBar.open(
      message,
      'Close',
      {
        duration: 3500,
        horizontalPosition: 'right',
        verticalPosition: 'bottom'
      }
    );
  }

  /*
   * =========================================================
   * CLEANUP
   * =========================================================
   */

  ngOnDestroy(): void {

    if (this.thumbnailBlobUrl) {

      URL.revokeObjectURL(
        this.thumbnailBlobUrl
      );
    }

    Object.values(
      this.screenshotBlobUrls
    ).forEach(
      (url: string) => {

        URL.revokeObjectURL(url);
      }
    );

    document.body.style.overflow =
      '';
  }
}