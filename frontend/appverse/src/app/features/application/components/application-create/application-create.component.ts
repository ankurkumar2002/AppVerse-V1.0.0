import { Component, OnInit } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule
} from '@angular/forms';

import {
  MatFormFieldModule,
  MatHint
} from '@angular/material/form-field';

import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import {
  MatSnackBar,
  MatSnackBarModule
} from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';

import {
  MatProgressBarModule
} from '@angular/material/progress-bar';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ApplicationRequest } from '../../models/application-request';
import { Category } from '../../../../models/category';
import { MonetizationType } from '../../../../models/monetization-type';

import { AuthService } from '../../../../core/services/auth/auth.service';
import { CategoryService } from '../../../../core/services/categories/category.service';
import { DeveloperService } from '../../../developer/services/developer.service';
import { ApplicationService } from '../../services/application.service';


@Component({
  selector: 'app-application-create',
  standalone: true,

  imports: [
    CommonModule,
    ReactiveFormsModule,

    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatSnackBarModule,
    MatIconModule,

    MatProgressBarModule,
    MatProgressSpinnerModule,

    TitleCasePipe,
    MatHint
  ],

  templateUrl: './application-create.component.html',
  styleUrls: ['./application-create.component.scss']
})
export class ApplicationCreateComponent implements OnInit {

  applicationForm: FormGroup;

  categories: Category[] = [];

  currencies = [
    'USD',
    'EUR',
    'GBP',
    'INR'
  ];

  monetizationTypes = Object.values(MonetizationType);

  availablePlatforms = [
    'WEB',
    'ANDROID',
    'IOS',
    'WINDOWS',
    'MACOS'
  ];


  thumbnailFile: File | null = null;

  screenshotFiles: File[] = [];

  thumbnailPreviewUrl: string | ArrayBuffer | null = null;


  /*
   * Upload state
   */

  isUploading = false;

  isUploadingThumbnail = false;

  isUploadingScreenshots = false;


  /*
   * Upload progress
   */

  thumbnailUploadProgress = 0;

  screenshotUploadProgress = 0;


  /*
   * Preview URLs for screenshots
   */

  screenshotPreviewUrls: Map<string, string> = new Map();


  constructor(
    private fb: FormBuilder,
    private applicationService: ApplicationService,
    private categoryService: CategoryService,
    private developerService: DeveloperService,
    private router: Router,
    private snackBar: MatSnackBar,
    private authService: AuthService
  ) {

    this.applicationForm = this.fb.group({

      name: [
        '',
        Validators.required
      ],

      tagline: [
        '',
        [
          Validators.maxLength(100)
        ]
      ],

      description: [
        '',
        [
          Validators.maxLength(2000)
        ]
      ],

      version: [
        '1.0.0'
      ],

      categoryId: [
        null,
        Validators.required
      ],

      monetizationType: [
        'FREE',
        Validators.required
      ],

      price: [
        null
      ],

      currency: [
        'USD'
      ],

      platforms: [
        [],
        Validators.required
      ]

    });


    const developerId = this.authService.getDeveloperId();

    if (developerId) {
      this.applicationForm.patchValue({
        developerId: developerId
      });
    }


    this.applicationForm
      .get('monetizationType')
      ?.valueChanges
      .subscribe(type => {

        this.updateValidators(type);

      });

  }


  ngOnInit(): void {

    this.loadCategories();

    this.updateValidators(
      this.applicationForm
        .get('monetizationType')
        ?.value
    );

  }


  private loadCategories(): void {

    this.categoryService.getAll().subscribe({

      next: categories => {

        this.categories = categories;

      },

      error: error => {

        console.error(
          'Failed to load categories:',
          error
        );

        this.snackBar.open(
          'Failed to load categories',
          'Close',
          {
            duration: 3000,
            panelClass: ['appverse-snackbar-error']
          }
        );

      }

    });

  }


  updateValidators(type: string): void {

    const priceControl =
      this.applicationForm.get('price');

    const currencyControl =
      this.applicationForm.get('currency');


    if (type === MonetizationType.FREE) {

      priceControl?.clearValidators();

      currencyControl?.clearValidators();

      priceControl?.setValue(null);

    } else {

      priceControl?.setValidators([
        Validators.required,
        Validators.min(0.01)
      ]);

      currencyControl?.setValidators([
        Validators.required
      ]);

    }


    priceControl?.updateValueAndValidity();

    currencyControl?.updateValueAndValidity();

  }


  onThumbnailSelected(event: Event): void {

    const input =
      event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      return;
    }


    const file = input.files[0];


    if (!this.isValidImage(file)) {

      this.showError(
        'Please select a PNG or JPG image.'
      );

      input.value = '';

      return;

    }


    this.thumbnailFile = file;


    const reader = new FileReader();


    reader.onload = () => {

      this.thumbnailPreviewUrl =
        reader.result;

    };


    reader.readAsDataURL(file);


    this.simulateThumbnailUpload();

  }


  private simulateThumbnailUpload(): void {

    this.isUploadingThumbnail = true;

    this.updateOverallUploadingState();

    this.thumbnailUploadProgress = 0;


    const interval = setInterval(() => {

      this.thumbnailUploadProgress += 10;


      if (this.thumbnailUploadProgress >= 100) {

        this.thumbnailUploadProgress = 100;

        clearInterval(interval);

        setTimeout(() => {

          this.isUploadingThumbnail = false;

          this.updateOverallUploadingState();

        }, 300);

      }

    }, 80);

  }


  onScreenshotsSelected(event: Event): void {

    const input =
      event.target as HTMLInputElement;


    if (!input.files || input.files.length === 0) {
      return;
    }


    const selectedFiles =
      Array.from(input.files);


    const validFiles: File[] = [];


    for (const file of selectedFiles) {

      if (!this.isValidImage(file)) {

        this.showError(
          `${file.name} is not a valid PNG or JPG image.`
        );

        continue;

      }


      validFiles.push(file);

    }


    if (validFiles.length === 0) {

      input.value = '';

      return;

    }


    this.screenshotFiles = [
      ...this.screenshotFiles,
      ...validFiles
    ];


    this.generateScreenshotPreviews(
      validFiles
    );


    this.simulateScreenshotUpload();

    input.value = '';

  }


  private generateScreenshotPreviews(
    files: File[]
  ): void {

    files.forEach(file => {

      const reader =
        new FileReader();


      reader.onload = () => {

        if (typeof reader.result === 'string') {

          this.screenshotPreviewUrls.set(
            this.getFileKey(file),
            reader.result
          );

        }

      };


      reader.readAsDataURL(file);

    });

  }


  getScreenshotPreview(
    file: File
  ): string | null {

    return (
      this.screenshotPreviewUrls.get(
        this.getFileKey(file)
      ) || null
    );

  }


  private getFileKey(file: File): string {

    return `${file.name}_${file.size}_${file.lastModified}`;

  }


  formatFileSize(
    bytes: number
  ): string {

    if (bytes === 0) {
      return '0 Bytes';
    }


    const units = [
      'Bytes',
      'KB',
      'MB',
      'GB'
    ];


    const index =
      Math.floor(
        Math.log(bytes) /
        Math.log(1024)
      );


    const size =
      bytes /
      Math.pow(1024, index);


    return `${size.toFixed(
      index === 0 ? 0 : 1
    )} ${units[index]}`;

  }


  removeScreenshot(index: number): void {

    if (this.isUploading) {
      return;
    }


    const file =
      this.screenshotFiles[index];


    if (file) {

      this.screenshotPreviewUrls.delete(
        this.getFileKey(file)
      );

    }


    this.screenshotFiles =
      this.screenshotFiles.filter(
        (_, i) => i !== index
      );

  }


  private simulateScreenshotUpload(): void {

    this.isUploadingScreenshots = true;

    this.updateOverallUploadingState();

    this.screenshotUploadProgress = 0;


    const interval = setInterval(() => {

      this.screenshotUploadProgress += 10;


      if (this.screenshotUploadProgress >= 100) {

        this.screenshotUploadProgress = 100;

        clearInterval(interval);


        setTimeout(() => {

          this.isUploadingScreenshots = false;

          this.updateOverallUploadingState();

        }, 300);

      }

    }, 100);

  }


  private updateOverallUploadingState(): void {

    this.isUploading =
      this.isUploadingThumbnail ||
      this.isUploadingScreenshots;

  }


  private isValidImage(file: File): boolean {

    const allowedTypes = [
      'image/png',
      'image/jpeg'
    ];


    return allowedTypes.includes(
      file.type
    );

  }


  onSubmit(): void {

    if (this.isUploading) {

      this.showError(
        'Please wait until all files finish uploading.'
      );

      return;

    }


    if (this.applicationForm.invalid) {

      this.applicationForm.markAllAsTouched();

      this.showError(
        'Please fill out all required fields correctly.'
      );

      return;

    }


    if (!this.thumbnailFile) {

      this.showError(
        'A thumbnail image is required.'
      );

      return;

    }


    const developerId =
      this.authService.getDeveloperId();


    if (!developerId) {

      this.showError(
        'Unable to determine developer identity. Please re-login.'
      );

      return;

    }


    const formValue =
      this.applicationForm.value;


    const request: ApplicationRequest = {

      name: formValue.name,

      tagline: formValue.tagline,

      description: formValue.description,

      version: formValue.version,

      categoryId: formValue.categoryId,

      price: formValue.price,

      currency: formValue.currency,

      isFree:
        formValue.monetizationType ===
        MonetizationType.FREE,

      monetizationType:
        formValue.monetizationType as MonetizationType,

      platforms:
        formValue.platforms

    };


    this.isUploading = true;


    this.applicationService
      .createApplication(
        request,
        this.thumbnailFile,
        this.screenshotFiles
      )
      .subscribe({

        next: createdApp => {

          this.isUploading = false;


          this.snackBar.open(
            'Application created successfully!',
            'OK',
            {
              duration: 3000,
              panelClass: ['appverse-snackbar-success']
            }
          );


          this.router.navigate([
            '/apps',
            'details',
            createdApp.id
          ]);

        },


        error: (err: HttpErrorResponse) => {

          this.isUploading = false;


          console.error(
            'Failed to create application:',
            err
          );


          const errorMessage =
            err.error?.message ||
            'Failed to create application';


          this.showError(
            errorMessage
          );

        }

      });

  }


  onCancel(): void {

    if (this.isUploading) {
      return;
    }


    this.router.navigate([
      '/dashboard/apps'
    ]);

  }


  private showError(
    message: string
  ): void {

    this.snackBar.open(
      message,
      'Close',
      {
        duration: 4000,
        panelClass: [
          'appverse-snackbar-error'
        ]
      }
    );

  }

}