import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ApplicationService } from '../../services/application.service';
import {
  ApplicationDetail,
  UpdateApplicationRequest,
  ScreenshotRequest,
  Screenshot
} from '../../models/application-detail';

@Component({
  selector: 'app-application-update',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './application-update.component.html',
  styleUrls: ['./application-update.component.scss']
})
export class ApplicationUpdateComponent implements OnInit, OnDestroy {
  updateForm!: FormGroup;

  appId = '';
  originalApplicationData!: ApplicationDetail;

  selectedThumbnail: File | null = null;
  selectedScreenshots: File[] = [];

  thumbnailPreviewUrl: SafeUrl | null = null;
  screenshotPreviewUrls: SafeUrl[] = [];

  private objectUrls: string[] = [];

  isLoading = true;
  isSubmitting = false;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private applicationService: ApplicationService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.appId = this.route.snapshot.paramMap.get('id') ?? '';

    if (!this.appId) {
      this.errorMessage = 'Application ID not found in URL.';
      this.isLoading = false;
      return;
    }

    this.updateForm = this.fb.group({
      name: ['', Validators.required],
      tagline: [''],
      description: ['', Validators.required],
      version: ['', Validators.required],
      categoryId: ['', Validators.required]
    });

    this.loadApplicationDetails();
  }

  ngOnDestroy(): void {
    this.revokeObjectUrls();
  }

  loadApplicationDetails(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.applicationService.getApplicationById(this.appId).subscribe({
      next: app => {
        this.originalApplicationData = app;

        this.updateForm.patchValue({
          name: app.name,
          tagline: app.tagline,
          description: app.description,
          version: app.version,
          categoryId: app.categoryId
        });

        this.loadAndDisplayExistingImages(app);

        this.isLoading = false;
      },
      error: err => {
        console.error('Failed to load application:', err);

        this.errorMessage =
          err?.error?.message ||
          'Failed to load application data. Please try again.';

        this.isLoading = false;
      }
    });
  }

  private loadAndDisplayExistingImages(app: ApplicationDetail): void {
    this.clearPreviewUrls();

    if (app.thumbnailUrl) {
      this.applicationService
        .getImageAsBlob('thumbnails', app.thumbnailUrl)
        .subscribe({
          next: blob => {
            const objectUrl = URL.createObjectURL(blob);

            this.objectUrls.push(objectUrl);

            this.thumbnailPreviewUrl =
              this.sanitizer.bypassSecurityTrustUrl(objectUrl);
          },
          error: err => {
            console.error('Failed to load thumbnail:', err);
          }
        });
    }

    this.screenshotPreviewUrls = [];

    (app.screenshots || []).forEach(screenshot => {
      const imagePath = this.getScreenshotImagePath(screenshot);

      if (!imagePath) {
        return;
      }

      this.applicationService
        .getImageAsBlob('screenshots', imagePath)
        .subscribe({
          next: blob => {
            const objectUrl = URL.createObjectURL(blob);

            this.objectUrls.push(objectUrl);

            this.screenshotPreviewUrls.push(
              this.sanitizer.bypassSecurityTrustUrl(objectUrl)
            );
          },
          error: err => {
            console.error('Failed to load screenshot:', err);
          }
        });
    });
  }

  private getScreenshotImagePath(screenshot: Screenshot): string {
    return screenshot.imageUrl || '';
  }

  onThumbnailSelect(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      return;
    }

    const file = input.files[0];

    this.selectedThumbnail = file;

    const objectUrl = URL.createObjectURL(file);

    this.objectUrls.push(objectUrl);

    this.thumbnailPreviewUrl =
      this.sanitizer.bypassSecurityTrustUrl(objectUrl);
  }

  onScreenshotsSelect(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      return;
    }

    this.selectedScreenshots = Array.from(input.files);

    this.screenshotPreviewUrls = this.selectedScreenshots.map(file => {
      const objectUrl = URL.createObjectURL(file);

      this.objectUrls.push(objectUrl);

      return this.sanitizer.bypassSecurityTrustUrl(objectUrl);
    });
  }

  onCancel(): void {
    this.router.navigate(['/developer/apps', this.appId]);
  }

  onSubmit(): void {
    if (this.updateForm.invalid) {
      this.updateForm.markAllAsTouched();
      return;
    }

    if (this.isSubmitting) {
      return;
    }

    if (!this.originalApplicationData) {
      this.errorMessage = 'Application data is not available.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    const metadataForUpload: ScreenshotRequest[] =
      this.selectedScreenshots.length > 0
        ? this.selectedScreenshots.map((file, index) => ({
            imageUrl: '',
            caption: file.name,
            order: index
          }))
        : (this.originalApplicationData.screenshots || []).map(
            (screenshot, index) => ({
              imageUrl: screenshot.imageUrl,
              caption: screenshot.caption || '',
              order: screenshot.order ?? index
            })
          );

    const updateRequestPayload: UpdateApplicationRequest = {
      name: this.updateForm.value.name,
      description: this.updateForm.value.description,
      version: this.updateForm.value.version,
      categoryId: this.updateForm.value.categoryId,

      thumbnailUrl: this.originalApplicationData.thumbnailUrl,
      developerId: this.originalApplicationData.developerId,
      status: (this.originalApplicationData as any).status || 'DRAFT',

      screenshots: metadataForUpload,

      price: Number(this.originalApplicationData.price) || 0,
      currency: (this.originalApplicationData as any).currency || 'INR',

      isFree: this.originalApplicationData.isFree,

      platforms: (this.originalApplicationData as any).platforms || [],

      accessUrl: (this.originalApplicationData as any).accessUrl || '',

      websiteUrl:
        (this.originalApplicationData as any).websiteUrl ||
        this.originalApplicationData.websiteUrl ||
        '',

      supportUrl: (this.originalApplicationData as any).supportUrl || '',

      tags: (this.originalApplicationData as any).tags || [],

      monetizationType:
        this.originalApplicationData.monetizationType || 'FREE'
    };

    this.applicationService
      .updateApplication(
        this.appId,
        updateRequestPayload,
        this.selectedThumbnail,
        this.selectedScreenshots,
        metadataForUpload
      )
      .subscribe({
        next: () => {
          this.isSubmitting = false;

          this.router.navigate([
            '/developer/apps',
            this.appId
          ]);
        },
        error: err => {
          console.error('Failed to update application:', err);

          this.errorMessage =
            err?.error?.message ||
            'An unknown error occurred while updating the application.';

          this.isSubmitting = false;
        }
      });
  }

  private clearPreviewUrls(): void {
    this.revokeObjectUrls();

    this.thumbnailPreviewUrl = null;
    this.screenshotPreviewUrls = [];
  }

  private revokeObjectUrls(): void {
    this.objectUrls.forEach(url => {
      URL.revokeObjectURL(url);
    });

    this.objectUrls = [];
  }
}