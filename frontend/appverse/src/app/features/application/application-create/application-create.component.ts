import { Component, OnInit } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule, MatHint } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { HttpErrorResponse } from '@angular/common/http';

import { ApplicationService } from '../../../core/services/application/application.service';
import { CategoryService } from '../../../core/services/categories/category.service';
import { DeveloperService } from '../../../core/services/developer/developer.service';
import { ApplicationRequest } from '../../../models/application-request';
import { Category } from '../../../models/category';
import { MonetizationType } from '../../../models/monetization-type';
import { AuthService } from '../../../core/services/auth/auth.service';

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
    TitleCasePipe,
    MatHint
  ],
  templateUrl: './application-create.component.html',
  styleUrls: ['./application-create.component.scss']
})
export class ApplicationCreateComponent implements OnInit {
  applicationForm: FormGroup;
  categories: Category[] = [];

  currencies = ['USD', 'EUR', 'GBP', 'INR'];
  monetizationTypes = Object.values(MonetizationType); 
  availablePlatforms = ['WEB', 'ANDROID', 'IOS', 'WINDOWS', 'MACOS'];

  thumbnailFile: File | null = null;
  screenshotFiles: File[] = [];
  thumbnailPreviewUrl: string | ArrayBuffer | null = null;

  constructor(
    private fb: FormBuilder,
    private applicationService: ApplicationService,
    private categoryService: CategoryService,
    private developerService: DeveloperService,
    private router: Router,
    private snackBar: MatSnackBar,
    private authService:AuthService
  ) {
    this.applicationForm = this.fb.group({
      name: ['', Validators.required],
      tagline: [''],
      description: [''],
      version: ['1.0.0'],
      categoryId: [null, Validators.required],
      monetizationType: ['FREE', Validators.required],
      price: [null],
      currency: ['USD'],
      platforms: [[], Validators.required]
    });
    const devId = this.authService.getDeveloperId();
  this.applicationForm.patchValue({ developerId: devId });

    this.applicationForm.get('monetizationType')?.valueChanges.subscribe(type => {
      this.updateValidators(type);
    });
  }

  ngOnInit(): void {
    this.categoryService.getAll().subscribe({
      next: (categories) => (this.categories = categories),
      error: () => this.snackBar.open('Failed to load categories', 'Close', { duration: 3000 })
    });

    this.updateValidators(this.applicationForm.get('monetizationType')?.value);
  }

  updateValidators(type: string): void {
    const priceControl = this.applicationForm.get('price');
    const currencyControl = this.applicationForm.get('currency');

    if (type === 'FREE') {
      priceControl?.clearValidators();
      currencyControl?.clearValidators();
      priceControl?.setValue(null);
    } else {
      priceControl?.setValidators([Validators.required, Validators.min(0.01)]);
      currencyControl?.setValidators(Validators.required);
    }

    priceControl?.updateValueAndValidity();
    currencyControl?.updateValueAndValidity();
  }

  onThumbnailSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.thumbnailFile = file;

      const reader = new FileReader();
      reader.onload = () => {
        this.thumbnailPreviewUrl = reader.result;
      };
      reader.readAsDataURL(file);
    }
  }

  onScreenshotsSelected(event: any): void {
    this.screenshotFiles = Array.from(event.target.files);
  }

  onSubmit(): void {
  if (this.applicationForm.invalid) {
    this.snackBar.open('Please fill out all required fields correctly.', 'Close', { duration: 3000 });
    this.applicationForm.markAllAsTouched();
    return;
  }

  if (!this.thumbnailFile) {
    this.snackBar.open('A thumbnail image is required.', 'Close', { duration: 3000 });
    return;
  }

  const devId = this.authService.getDeveloperId(); // ✅ Get Keycloak ID
  console.log(devId);
  if (!devId) {
    this.snackBar.open('Unable to determine developer identity. Please re-login.', 'Close', { duration: 3000 });
    return;
  }


  const formValue = this.applicationForm.value;

  const request: ApplicationRequest = {
    name: formValue.name,
    tagline: formValue.tagline,
    description: formValue.description,
    version: formValue.version,
    categoryId: formValue.categoryId,
    price: formValue.price,
    currency: formValue.currency,
    isFree: formValue.monetizationType === MonetizationType.FREE, // ✅ derived from enum
    monetizationType: formValue.monetizationType as MonetizationType, // ✅ cast safely
    platforms: formValue.platforms,
    developerId: devId
    // optionally add: offeredSubscriptionPlans, accessUrl, websiteUrl, etc.
  };

  this.applicationService.createApplication(request, this.thumbnailFile, this.screenshotFiles).subscribe({
    next: (createdApp) => {
      this.snackBar.open('Application created successfully!', 'OK', { duration: 3000 });
      this.router.navigate(['/apps', 'details', createdApp.id]);
    },
    error: (err: HttpErrorResponse) => {
      const errorMessage = err.error?.message || 'Failed to create application';
      this.snackBar.open(`Error: ${errorMessage}`, 'Close', { duration: 5000 });
    }
  });
}

  onCancel(): void {
    this.router.navigate(['/dashboard/apps']);
  }
}
