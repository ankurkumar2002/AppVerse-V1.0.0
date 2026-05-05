import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { trigger, style, animate, transition } from '@angular/animations';

import { DeveloperService } from '../../services/developer.service';
import { MessageService } from 'primeng/api';
import { getKeycloak } from '../../../../core/auth/keycloak';

// PrimeNG
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { TabViewModule } from 'primeng/tabview';

@Component({
  selector: 'app-developer-profile-update',
  standalone: true,
  templateUrl: './developer-profile-update.component.html',
  styleUrls: ['./developer-profile-update.component.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    DropdownModule,
    TabViewModule,
    ButtonModule,
    ToastModule,
    InputTextModule
  ],
  providers: [MessageService],
  animations: [
    trigger('fade', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('300ms ease-in', style({ opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-out', style({ opacity: 0 }))
      ])
    ])
  ]
})
export class DeveloperProfileUpdateComponent implements OnInit {
  developerForm: FormGroup;
  loading = false;
  logoPreviewUrl: string | null = null;

  developerTypes = [
    { label: 'Individual', value: 'INDIVIDUAL' },
    { label: 'Company', value: 'COMPANY' }
  ];

  constructor(
    private fb: FormBuilder,
    private developerService: DeveloperService,
    private messageService: MessageService
  ) {
    this.developerForm = this.fb.group({
      name: ['', Validators.required],
      email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
      website: [''],
      companyName: [''],
      bio: [''],
      logoUrl: [''],
      location: [''],
      developerType: ['INDIVIDUAL', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadDeveloperProfile();

    this.developerForm.get('logoUrl')?.valueChanges.subscribe(url => {
      this.logoPreviewUrl = url;
    });
  }

  loadDeveloperProfile(): void {
    this.loading = true;

    this.developerService.getMyDeveloperProfile().subscribe({
      next: developer => {
  this.developerForm.patchValue({
    name: `${developer.firstName ?? ''} ${developer.lastName ?? ''}`.trim(),
    email: developer.email,
    website: developer.website,
    companyName: developer.companyName,
    bio: developer.bio,
    logoUrl: developer.logoUrl,
    location: developer.location,
    developerType: developer.developerType
  });

  this.logoPreviewUrl = developer.logoUrl ?? null;
  this.loading = false;
},
      error: async () => {
        this.messageService.add({
          severity: 'info',
          summary: 'Welcome!',
          detail: 'Please complete your developer profile to continue.'
        });

        await this.loadKeycloakFallbacks();
        this.loading = false;
      }
    });
  }

  private async loadKeycloakFallbacks(): Promise<void> {
    try {
      const profile = await getKeycloak().loadUserProfile();

      const currentName = this.developerForm.get('name')?.value;
      if (!currentName) {
        this.developerForm.patchValue({
          name: `${profile.firstName ?? ''} ${profile.lastName ?? ''}`.trim()
        });
      }

      this.developerForm.patchValue({
        email: profile.email ?? ''
      });
    } catch (err) {
      console.error('Failed to load Keycloak profile', err);
    }
  }

  updateDeveloperProfile(): void {
    if (this.developerForm.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Invalid Form',
        detail: 'Please fill out all required fields.',
      });
      return;
    }

    this.loading = true;

    this.developerService
      .updateMyProfile(this.developerForm.getRawValue())
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Profile updated successfully!'
          });
          this.loading = false;
        },
        error: err => {
          console.error('Update failed:', err);
          this.messageService.add({
            severity: 'error',
            summary: 'Update Failed',
            detail: 'Could not save your profile. Please try again.'
          });
          this.loading = false;
        }
      });
  }
}
