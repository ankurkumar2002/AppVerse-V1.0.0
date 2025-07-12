// src/app/pages/developer-dashboard/developer-profile-update.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { DeveloperService } from '../../core/developer/developer.service';
import { KeycloakService } from 'keycloak-angular';
import { MessageService } from 'primeng/api';
import { DeveloperResponse } from '../../models/developer-response';

// PrimeNG Modules
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-developer-profile-update',
  standalone: true,
  templateUrl: './developer-profile-update.component.html',
  styleUrls: ['./developer-profile-update.component.scss'],
  imports: [
    ReactiveFormsModule,
    InputTextModule,
    DropdownModule,
    ButtonModule,
    ToastModule,
  ],
  providers: [MessageService]
})
export class DeveloperProfileUpdateComponent implements OnInit {
  developerForm: FormGroup;
  developer: DeveloperResponse | null = null;
  loading = false;

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
      email: ['', [Validators.required, Validators.email]],
      website: [''],
      companyName: [''],
      bio: [''],
      logoUrl: [''],
      location: [''],
      developerType: ['', Validators.required],
      role: ['DEVELOPER', Validators.required] // NEW
    });

  }

  ngOnInit(): void {
    this.loadDeveloperProfile();
  }

  loadDeveloperProfile(): void {
    this.loading = true;
    this.developerService.getMyDeveloperProfile().subscribe({
      next: (developer) => {
        this.developer = developer;
        this.developerForm.patchValue(developer);
        this.loadKeycloakNameFallback();
      },
      error: (err: any) => {
        console.error('Error loading developer profile:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load developer profile',
        });
        this.loading = false;
      },
    });
  }

  loadKeycloakNameFallback(): void {
    this.keycloakService.loadUserProfile()
      .then((profile) => {
        if (!this.developerForm.get('name')?.value) {
          const name = `${profile.firstName || ''} ${profile.lastName || ''}`.trim();
          this.developerForm.patchValue({ name });
        }
        if (!this.developerForm.get('email')?.value) {
          this.developerForm.patchValue({ email: profile.email || '' });
        }
        this.loading = false;
      })
      .catch((err: any) => {
        console.error('Error loading Keycloak fallback profile:', err);
        this.loading = false;
      });
  }

  updateDeveloperProfile(): void {
    if (this.developerForm.invalid || !this.developer) return;

    this.loading = true;
    console.log('Updating profile with:', this.developerForm.value);
    this.developerService.updateMyProfile(this.developerForm.value).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Developer profile updated successfully',
        });
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error updating developer profile:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to update developer profile',
        });
        this.loading = false;
      },
    });
  }
}
