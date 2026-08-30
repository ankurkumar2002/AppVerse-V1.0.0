import {
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { RouterModule } from '@angular/router';

import {
  ApplicationDetail,
  Screenshot
} from '../../../application/models/application-detail';

import {
  ApplicationService
} from '../../../application/services/application.service';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';


@Component({
  selector: 'app-app-detail',

  standalone: true,

  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule
  ],

  templateUrl: './app-detail.component.html',

  styleUrl: './app-detail.component.scss'
})
export class AppDetailComponent
  implements OnInit, OnDestroy {

  // ==========================================================
  // DATA
  // ==========================================================

  appId!: string;

  application!: ApplicationDetail;


  // ==========================================================
  // IMAGES
  // ==========================================================

  thumbnailBlobUrl: string | null = null;

  screenshotBlobUrls: {
    [key: string]: string
  } = {};


  // ==========================================================
  // STATE
  // ==========================================================

  isLoading = true;

  errorMessage = '';


  constructor(
    private appService: ApplicationService,
    private route: ActivatedRoute
  ) {}


  // ==========================================================
  // INIT
  // ==========================================================

  ngOnInit(): void {

    this.appId =
      this.route.snapshot.paramMap.get('id')!;

    if (!this.appId) {

      this.isLoading = false;

      this.errorMessage =
        'Application ID was not provided.';

      return;

    }

    this.loadApplicationDetails();

  }


  // ==========================================================
  // LOAD APPLICATION
  // ==========================================================

  loadApplicationDetails(): void {

    this.isLoading = true;

    this.errorMessage = '';

    this.appService
      .getApplicationById(this.appId)
      .subscribe({

        next: (app) => {

          this.application = app;

          this.isLoading = false;


          // -----------------------------------------------
          // THUMBNAIL
          // -----------------------------------------------

          this.loadThumbnailImage(
            app.thumbnailUrl
          );


          // -----------------------------------------------
          // SCREENSHOTS
          // -----------------------------------------------

          this.loadScreenshotImages(
            app.screenshots
          );

        },


        error: (err) => {

          console.error(
            'Failed to load application details:',
            err
          );

          this.isLoading = false;

          this.errorMessage =
            'Unable to load this application.';

        }

      });

  }


  // ==========================================================
  // THUMBNAIL
  // ==========================================================

  private loadThumbnailImage(
    imagePath?: string
  ): void {

    if (!imagePath) {

      this.thumbnailBlobUrl = null;

      return;

    }


    const filename =
      this.extractFileName(imagePath);


    if (!filename) {

      this.thumbnailBlobUrl = null;

      return;

    }


    this.appService
      .getImageAsBlob(
        'thumbnails',
        filename
      )
      .subscribe({

        next: (blob) => {

          this.thumbnailBlobUrl =
            URL.createObjectURL(blob);

        },


        error: (err) => {

          console.error(
            `Error fetching thumbnail '${filename}':`,
            err
          );

          this.thumbnailBlobUrl = null;

        }

      });

  }


  // ==========================================================
  // SCREENSHOTS
  // ==========================================================

  private loadScreenshotImages(
    screenshots?: Screenshot[]
  ): void {

    if (!screenshots?.length) {

      return;

    }


    screenshots.forEach(
      (screenshot: Screenshot) => {

        if (!screenshot.imageUrl) {

          return;

        }


        const filename =
          this.extractFileName(
            screenshot.imageUrl
          );


        if (!filename) {

          return;

        }


        this.appService
          .getImageAsBlob(
            'screenshots',
            filename
          )
          .subscribe({

            next: (blob) => {

              this.screenshotBlobUrls[
                screenshot._id
              ] =
                URL.createObjectURL(blob);

            },


            error: (err) => {

              console.error(
                `Error fetching screenshot '${filename}':`,
                err
              );

            }

          });

      }
    );

  }


  // ==========================================================
  // FILE NAME
  // ==========================================================

  extractFileName(
    fullPath: string
  ): string {

    if (!fullPath) {

      return '';

    }


    /*
     * Handles both:
     *
     * /uploads/image.png
     *
     * C:\uploads\image.png
     */

    const normalized =
      fullPath.replace(/\\/g, '/');


    return (
      normalized
        .split('/')
        .pop() || ''
    );

  }


  // ==========================================================
  // CLEANUP
  // ==========================================================

  ngOnDestroy(): void {

    if (this.thumbnailBlobUrl) {

      URL.revokeObjectURL(
        this.thumbnailBlobUrl
      );

    }


    Object
      .values(this.screenshotBlobUrls)
      .forEach(url => {

        URL.revokeObjectURL(url);

      });

  }

}