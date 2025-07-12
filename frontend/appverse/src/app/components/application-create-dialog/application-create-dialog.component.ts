import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms'; // Added ReactiveFormsModule
import { ApplicationService } from '../../core/application/application.service';
import { ApplicationRequest } from '../../models/application-request';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox'; // Added for mat-checkbox
import { SubscriptionPlanService } from '../../core/subscription/subscription-plan.service';
import { of } from 'rxjs';

@Component({
  selector: 'app-application-create-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule, // Added for mat-checkbox
    ReactiveFormsModule // Added for formGroup
  ],
  templateUrl: './application-create-dialog.component.html',
  styleUrls: ['./application-create-dialog.component.scss']
})
export class ApplicationCreateDialogComponent {
  applicationForm: FormGroup;
  currencies = ['USD', 'EUR', 'GBP', 'INR'];
  billingIntervals = ['DAY', 'WEEK', 'MONTH', 'QUARTER', 'YEAR'];

  constructor(
    public dialogRef: MatDialogRef<ApplicationCreateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder,
    private applicationService: ApplicationService,
    private subscriptionPlanService: SubscriptionPlanService
  ) {
    this.applicationForm = this.fb.group({
      name: ['', Validators.required],
      tagline: [''],
      description: [''],
      version: ['1.0.0'],
      categoryId: [''],
      isFree: [true],
      monetizationType: ['FREE'],
      price: [0, [Validators.min(0)]],
      currency: ['USD'],
      billingInterval: ['MONTH'],
      billingIntervalCount: [1, [Validators.min(1)]],
      trialPeriodDays: [0, [Validators.min(0)]]
    });

    this.applicationForm.get('isFree')?.valueChanges.subscribe(isFree => {
      const priceControl = this.applicationForm.get('price');
      const currencyControl = this.applicationForm.get('currency');
      const billingIntervalControl = this.applicationForm.get('billingInterval');
      const billingIntervalCountControl = this.applicationForm.get('billingIntervalCount');
      const trialPeriodDaysControl = this.applicationForm.get('trialPeriodDays');

      if (isFree) {
        priceControl?.clearValidators();
        currencyControl?.clearValidators();
        billingIntervalControl?.clearValidators();
        billingIntervalCountControl?.clearValidators();
        trialPeriodDaysControl?.clearValidators();
        this.applicationForm.get('monetizationType')?.setValue('FREE');
        priceControl?.setValue(0);
      } else {
        priceControl?.setValidators([Validators.required, Validators.min(0.01)]);
        currencyControl?.setValidators([Validators.required]);
        billingIntervalControl?.setValidators([Validators.required]);
        billingIntervalCountControl?.setValidators([Validators.required, Validators.min(1)]);
        trialPeriodDaysControl?.setValidators([Validators.min(0)]);
        this.applicationForm.get('monetizationType')?.setValue('SUBSCRIPTION');
      }
      priceControl?.updateValueAndValidity();
      currencyControl?.updateValueAndValidity();
      billingIntervalControl?.updateValueAndValidity();
      billingIntervalCountControl?.updateValueAndValidity();
      trialPeriodDaysControl?.updateValueAndValidity();
    });
  }

  onSubmit(): void {
    if (this.applicationForm.invalid) return;

    const formValue = this.applicationForm.value;
    const request: ApplicationRequest = {
      name: formValue.name,
      tagline: formValue.tagline,
      description: formValue.description,
      version: formValue.version,
      categoryId: formValue.categoryId,
      price: formValue.isFree ? 0 : formValue.price,
      currency: formValue.currency,
      isFree: formValue.isFree,
      monetizationType: formValue.monetizationType
    };

    this.applicationService.createApplication(request).subscribe({
      next: (response) => {
        if (!formValue.isFree) {
          const planRequest = {
            planNameKey: `${formValue.name.toLowerCase().replace(/\s+/g, '-')}-plan`,
            displayName: `${formValue.name} Subscription`,
            description: formValue.description || 'Subscription for ' + formValue.name,
            price: formValue.price,
            currency: formValue.currency,
            billingInterval: formValue.billingInterval,
            billingIntervalCount: formValue.billingIntervalCount,
            trialPeriodDays: formValue.trialPeriodDays,
            applicationId: response.id,
            developerId: 'mock-developer-id'
          };
          this.subscriptionPlanService.createDeveloperPlan(planRequest).subscribe({
            next: () => {
              console.log('Subscription plan created successfully');
              this.dialogRef.close(true);
            },
            error: (err) => {
              console.error('Error creating subscription plan:', err);
              this.dialogRef.close(false);
            }
          });
        } else {
          console.log('Free application created, no subscription plan needed');
          this.dialogRef.close(true);
        }
      },
      error: (err) => {
        console.error('Error creating application:', err);
        this.dialogRef.close(false);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}