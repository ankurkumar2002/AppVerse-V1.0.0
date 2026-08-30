import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { UserAuthService } from '../../services/user/user-auth.service';
import { UserDetailsResponse } from '../../models/UserDetailsResponse';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-edit-user',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './edit-user.component.html',
  styleUrl: './edit-user.component.scss'
})
export class EditUserComponent implements OnInit {

  profileForm!: FormGroup;

  userDetails!: UserDetailsResponse;

  isLoading = false;
  isSaving = false;


  constructor(
    private fb: FormBuilder,
    private userService: UserAuthService
  ) {}


  ngOnInit(): void {

    this.profileForm = this.fb.group({

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
      ],

      phone: ['']

    });

    this.loadProfile();
  }


  loadProfile(): void {

    this.isLoading = true;

    this.userService
      .getUserDetailsByKeycloakId()
      .subscribe({

        next: data => {

          this.userDetails = data;

          this.profileForm.patchValue({

            firstName: data.firstName,
            lastName: data.lastName,
            email: data.email,
            phone: data.phone

          });

          this.isLoading = false;
        },

        error: err => {

          console.error(
            'Error fetching user details:',
            err
          );

          this.isLoading = false;
        }

      });

  }


  updateProfile(): void {

    if (this.profileForm.invalid) {

      this.profileForm.markAllAsTouched();

      return;
    }


    this.isSaving = true;

    const payload = this.profileForm.value;

    console.log(
      'PAYLOAD SENT:',
      payload
    );


    this.userService
      .updateUserProfile(payload)
      .subscribe({

        next: response => {

          console.log(
            'Profile updated successfully:',
            response
          );

          this.isSaving = false;

        },

        error: err => {

          console.error(
            'Failed to update profile:',
            err
          );

          this.isSaving = false;

        }

      });

  }

}