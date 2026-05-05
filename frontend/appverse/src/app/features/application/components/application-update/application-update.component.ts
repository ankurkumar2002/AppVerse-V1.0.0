import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ApplicationService } from '../../services/application.service';
import { ApplicationDetail, UpdateApplicationRequest, ScreenshotRequest } from '../../models/application-detail'; // Adjust path as needed

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
export class ApplicationUpdateComponent implements OnInit {
  updateForm!: FormGroup;
  appId!: string;
  originalApplicationData!: ApplicationDetail;

  // For managing new file selections
  selectedThumbnail: File | null = null;
  selectedScreenshots: File[] = [];

  // For securely displaying image previews
  thumbnailPreviewUrl: SafeUrl | null = null;
  screenshotPreviewUrls: SafeUrl[] = [];

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
    this.appId = this.route.snapshot.paramMap.get('id')!;
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
      categoryId: ['', Validators.required],
    });

    this.loadApplicationDetails();
  }

  loadApplicationDetails(): void {
    this.applicationService.getApplicationById(this.appId).subscribe({
      next: (app) => {
        this.originalApplicationData = app;
        
        this.updateForm.patchValue({
          name: app.name,
          tagline: app.tagline,
          description: app.description,
          version: app.version,
          categoryId: app.categoryId,
        });

        this.loadAndDisplayExistingImages(app);
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load application data. Please try again.';
        this.isLoading = false;
      }
    });
  }
  
  private loadAndDisplayExistingImages(app: ApplicationDetail): void {
    if (app.thumbnailUrl) {
      const filename = this.extractFileName(app.thumbnailUrl);
      if (filename) {
        this.applicationService.getImageAsBlob('thumbnails', filename).subscribe({
          next: (blob) => {
            const objectURL = URL.createObjectURL(blob);
            this.thumbnailPreviewUrl = this.sanitizer.bypassSecurityTrustUrl(objectURL);
          },
          error: (err) => console.error(`Error fetching thumbnail '${filename}':`, err)
        });
      }
    }

    this.screenshotPreviewUrls = [];
    (app.screenshots || []).forEach(screenshot => {
      const filename = this.extractFileName(screenshot.imageUrl);
      if (filename) {
        this.applicationService.getImageAsBlob('screenshots', filename).subscribe({
          next: (blob) => {
            const objectURL = URL.createObjectURL(blob);
            this.screenshotPreviewUrls.push(this.sanitizer.bypassSecurityTrustUrl(objectURL));
          },
          error: (err) => console.error(`Error fetching screenshot '${filename}':`, err)
        });
      }
    });
  }

  private extractFileName(fullPath: string): string {
    if (!fullPath) return '';
    return fullPath.split('/').pop() || '';
  }

  onThumbnailSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedThumbnail = input.files[0];
      this.thumbnailPreviewUrl = this.sanitizer.bypassSecurityTrustUrl(
        URL.createObjectURL(this.selectedThumbnail)
      );
    }
  }

  onScreenshotsSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.selectedScreenshots = Array.from(input.files);
      this.screenshotPreviewUrls = this.selectedScreenshots.map(file => 
        this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(file))
      );
    }
  }
  
  public onCancel(): void {
    this.router.navigate(['/apps', this.appId]);
  }

  onSubmit(): void {
    if (this.updateForm.invalid) {
      this.updateForm.markAllAsTouched();
      return;
    }
    if (this.isSubmitting) return;

    this.isSubmitting = true;
    this.errorMessage = null;

    // --- Build Request Payloads with the BUG FIX ---

    // 1. Start with a safe payload that preserves original data, including screenshots.
    const updateRequestPayload: UpdateApplicationRequest = {
      ...this.originalApplicationData,
      ...this.updateForm.value,
    };
    
    let metadataForUpload: ScreenshotRequest[] = [];

    // 2. Check if the user has selected NEW screenshot files.
    if (this.selectedScreenshots.length > 0) {
      // ONLY if new files are selected, do we prepare to replace the old list.
      
      // A. Build the dummy metadata for the new files.
      metadataForUpload = this.selectedScreenshots.map((file, index) => ({
        imageUrl: "", // The "lie" to satisfy the backend DTO
        caption: `Screenshot ${index + 1}`,
        order: index,
      }));

      // B. Overwrite the 'screenshots' property on our payload with this new dummy list.
      updateRequestPayload.screenshots = metadataForUpload;
    }
    // If no new files were selected, we do nothing, and the original screenshot list
    // from '...this.originalApplicationData' remains on the payload.

    // 3. Call the service.
    this.applicationService.updateApplication(
      this.appId,
      updateRequestPayload,
      this.selectedThumbnail,
      this.selectedScreenshots,
      metadataForUpload
    ).subscribe({
      next: () => {
        alert('Application updated successfully!');
        this.isSubmitting = false;
        this.router.navigate(['/apps', this.appId]);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'An unknown error occurred during the update.';
        this.isSubmitting = false;
      }
    });
  }
}