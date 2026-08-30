import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { updateDeveloperRequest } from '../../models/update-developer-request';
import { DeveloperService } from '../../services/developer.service';
import { NotificationService } from '../../../user/services/notificationService/NotificationService';

@Component({
  selector: 'app-update-personel-details',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule
  ],
  templateUrl: './update-personel-details.component.html',
  styleUrl: './update-personel-details.component.scss'
})
export class UpdatePersonelDetailsComponent implements OnInit {

  updateDeveloperRequest!: updateDeveloperRequest;

  developerProfileForm!: FormGroup;

  isLoading = false;
  isSaving = false;


  constructor(
    private fb: FormBuilder,
    private developerService: DeveloperService,
    private notificationService: NotificationService,
    private router: Router
  ) {}


  ngOnInit(): void {

    this.developerProfileForm = this.fb.group({

      firstName: [
        '',
        Validators.required
      ],

      lastName: [
        '',
        Validators.required
      ],

      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ]

    });

    this.loadDeveloperProfile();
  }


  // =========================================================
  // LOAD PROFILE
  // =========================================================

  loadDeveloperProfile(): void {

    this.isLoading = true;

    this.developerService.getMyDeveloperProfile().subscribe({

      next: (data) => {

        this.updateDeveloperRequest = data;

        this.developerProfileForm.patchValue({

          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email

        });

        this.isLoading = false;

      },

      error: (err) => {

        console.error(
          'Error fetching developer profile:',
          err
        );

        this.isLoading = false;

        this.notificationService.error(
          err?.error?.message ??
          'Failed to load developer profile.'
        );

      }

    });

  }


  // =========================================================
  // UPDATE PROFILE
  // =========================================================

  updateProfile(): void {

    if (this.developerProfileForm.invalid) {

      this.developerProfileForm.markAllAsTouched();

      return;
    }


    if (this.isSaving) {
      return;
    }


    this.isSaving = true;


    const payload: updateDeveloperRequest =
      this.developerProfileForm.getRawValue();


    console.log('PAYLOAD SENT:');
    console.log(payload);


    this.developerService
      .updateDeveloperProfile(payload)
      .subscribe({

        next: (response) => {

          console.log(
            'Developer profile updated:',
            response
          );

          this.isSaving = false;

          this.notificationService.success(
            response?.message ??
            response?.str ??
            'Profile updated successfully.'
          );

          /*
           * Navigate back to developer profile.
           *
           * Change this route if your actual
           * developer profile route is different.
           */
          this.router.navigate([
            '/developer/profile'
          ]);

        },

        error: (err) => {

          console.error(
            'Error updating developer profile:',
            err
          );

          this.isSaving = false;

          const message =
            err?.error?.message ??
            err?.error?.str ??
            'Failed to update profile.';

          this.notificationService.error(message);

        }

      });

  }


  // =========================================================
  // CANCEL
  // =========================================================

  cancel(): void {

    if (this.isSaving) {
      return;
    }

    this.router.navigate([
      '/developer/profile'
    ]);

  }

}