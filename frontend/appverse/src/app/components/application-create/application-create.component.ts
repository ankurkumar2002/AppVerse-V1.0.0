import { Component, NgModule, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatRadioModule } from '@angular/material/radio';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { ApplicationService } from '../../core/application/application.service';
import { CategoryService } from '../../core/categories/category.service';
import { AuthService } from '../../core/auth/auth.service';
import { DeveloperService } from '../../core/developer/developer.service';

import { ApplicationRequest } from '../../models/application-request';
import { Category } from '../../models/category';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-application-create',
  standalone: true,
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatRadioModule,
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  templateUrl: './application-create.component.html',
  styleUrls: ['./application-create.component.scss']
})
export class ApplicationCreateComponent implements OnInit {
  applicationForm: FormGroup;
  categories: Category[] = [];

  currencies = ['USD', 'EUR', 'GBP', 'INR'];
  monetizationTypes = ['FREE', 'SUBSCRIPTION', 'ONE_TIME_PURCHASE'];
  billingIntervals = ['DAY', 'WEEK', 'MONTH', 'QUARTER', 'YEAR'];

  thumbnailFile: File | null = null;
  screenshotFiles: File[] = [];
  screenshotMetadata: { caption: string; order: number }[] = [];

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
      name: ['', Validators.required],
      tagline: [''],
      description: [''],
      version: ['1.0.0'],
      categoryId: [null, Validators.required],
      monetizationType: ['FREE'],
      price: [0, [Validators.min(0)]],
      currency: ['USD'],
      billingInterval: ['MONTH'],
      billingIntervalCount: [1, [Validators.min(1)]],
      trialPeriodDays: [0, [Validators.min(0)]]
    });

    this.applicationForm.get('monetizationType')?.valueChanges.subscribe(type => {
      const price = this.applicationForm.get('price');
      const currency = this.applicationForm.get('currency');
      const billingInterval = this.applicationForm.get('billingInterval');
      const billingIntervalCount = this.applicationForm.get('billingIntervalCount');
      const trial = this.applicationForm.get('trialPeriodDays');

      if (type === 'FREE') {
        price?.setValue(0);
        price?.clearValidators();
        currency?.clearValidators();
        billingInterval?.clearValidators();
        billingIntervalCount?.clearValidators();
        trial?.clearValidators();
      } else if (type === 'ONE_TIME_PURCHASE') {
        price?.setValidators([Validators.required, Validators.min(0.01)]);
        currency?.setValidators([Validators.required]);
        billingInterval?.clearValidators();
        billingIntervalCount?.clearValidators();
        trial?.clearValidators();
      } else if (type === 'SUBSCRIPTION') {
        price?.setValidators([Validators.required, Validators.min(0.01)]);
        currency?.setValidators([Validators.required]);
        billingInterval?.setValidators([Validators.required]);
        billingIntervalCount?.setValidators([Validators.required, Validators.min(1)]);
        trial?.setValidators([Validators.min(0)]);
      }

      price?.updateValueAndValidity();
      currency?.updateValueAndValidity();
      billingInterval?.updateValueAndValidity();
      billingIntervalCount?.updateValueAndValidity();
      trial?.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    this.categoryService.getAll().subscribe({
      next: (categories) => (this.categories = categories),
      error: () => {
        this.snackBar.open('Failed to load categories', 'Close', { duration: 3000 });
      }
    });

    this.developerService.getMyDeveloperProfile().subscribe({
      next: (dev) => console.log('Developer profile:', dev),
      error: () => this.snackBar.open('Please login again', 'Close', { duration: 3000 })
    });
  }

  onThumbnailSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.thumbnailFile = file;
    }
  }

  onScreenshotsSelected(event: any): void {
    this.screenshotFiles = Array.from(event.target.files);
    this.screenshotMetadata = this.screenshotFiles.map((_, index) => ({
      caption: '',
      order: index
    }));
  }

  onSubmit(): void {
    if (this.applicationForm.invalid) return;

    const dev = this.developerService.getProfile();
    const developerId = dev?.id;
    if (!developerId) {
      this.snackBar.open('Developer not found. Please login.', 'Close', { duration: 3000 });
      return;
    }

    const formValue = this.applicationForm.value;

    const request: ApplicationRequest = {
      name: formValue.name,
      tagline: formValue.tagline,
      description: formValue.description,
      version: formValue.version,
      categoryId: formValue.categoryId,
      price: formValue.monetizationType === 'FREE' ? 0 : formValue.price,
      currency: formValue.currency,
      isFree: formValue.monetizationType === 'FREE',
      monetizationType: formValue.monetizationType,
      developerId: developerId
    };

    this.applicationService
      .createApplication(request, this.thumbnailFile ?? undefined, this.screenshotFiles, this.screenshotMetadata)
      .subscribe({
        next: (res) => {
          this.snackBar.open('Application created successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/apps']);
        },
        error: (err: HttpErrorResponse) => {
          console.error(err);
          this.snackBar.open('Failed to create application', 'Close', { duration: 3000 });
        }
      });
  }

  onCancel(): void {
    this.router.navigate(['/apps']);
  }
}
