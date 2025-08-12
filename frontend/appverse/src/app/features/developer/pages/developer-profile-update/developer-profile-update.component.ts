// src/app/pages/developer-dashboard/developer-profile-update.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { trigger, style, animate, transition } from '@angular/animations'; // For animations
import { DeveloperService } from '../../../../core/services/developer/developer.service';
import { KeycloakService } from 'keycloak-angular';
import { MessageService } from 'primeng/api';
import { DeveloperResponse } from '../../../../models/developer-response'; // Assuming this path is correct

// PrimeNG Modules
import { InputTextModule } from 'primeng/inputtext';
// ✅ FIX: The separate 'InputTextareaModule' is no longer needed. Removed import.
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
    InputTextModule, // This module now includes the directive for <textarea pInputTextarea>
    // ✅ FIX: Removed InputTextareaModule from here.
    DropdownModule,
    TabViewModule,
    ButtonModule,
    ToastModule,
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
    private keycloakService: KeycloakService,
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
      next: (developer) => {
        this.developerForm.patchValue(developer);
        this.logoPreviewUrl = developer.logoUrl ?? null;
        this.loading = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'info',
          summary: 'Welcome!',
          detail: 'Please complete your developer profile to continue.'
        });
        this.loadKeycloakFallbacks();
        this.loading = false;
      },
    });
  }

  loadKeycloakFallbacks(): void {
    this.keycloakService.loadUserProfile().then(profile => {
      const formName = this.developerForm.get('name')?.value;
      if (!formName) {
        this.developerForm.patchValue({ name: `${profile.firstName || ''} ${profile.lastName || ''}`.trim() });
      }
      this.developerForm.patchValue({ email: profile.email || '' });
    }).catch(err => console.error('Failed to load Keycloak profile', err));
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
    this.developerService.updateMyProfile(this.developerForm.getRawValue()).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Profile updated successfully!' });
        this.loading = false;
      },
      error: (err) => {
        console.error('Update failed:', err);
        this.messageService.add({ severity: 'error', summary: 'Update Failed', detail: 'Could not save your profile. Please try again.' });
        this.loading = false;
      },
    });
  }
}