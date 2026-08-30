import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { RouterLink } from '@angular/router';

import { MessageResponse } from '../../../../models/message-response';

import { UserAuthService } from '../../services/user/user-auth.service';

import { passwordMatchValidator } from '../../../shared/passwordMatchValidator';

import { NotificationService } from '../../services/notificationService/NotificationService';


@Component({
  selector: 'app-edit-password',
  standalone: true,

  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],

  templateUrl: './edit-password.component.html',
  styleUrl: './edit-password.component.scss'
})
export class EditPasswordComponent implements OnInit {

  response?: MessageResponse;

  passwordForm!: FormGroup;

  isSaving = false;


  constructor(
    private notificationService: NotificationService,
    private fb: FormBuilder,
    private userService: UserAuthService
  ) {}


  ngOnInit(): void {

    this.passwordForm = this.fb.group({

      currentPassword: [
        '',
        Validators.required
      ],

      newPassword: [
        '',
        Validators.required
      ],

      confirmPassword: [
        '',
        Validators.required
      ]

    }, {

      validators: passwordMatchValidator()

    });

  }


  updatePassword(): void {

    if (this.passwordForm.invalid) {

      this.passwordForm.markAllAsTouched();

      return;
    }


    this.isSaving = true;


    const payload = this.passwordForm.value;

    console.log(
      'PASSWORD UPDATE REQUEST'
    );


    this.userService
      .updateUserPassword(payload)
      .subscribe({

        next: response => {

          this.response = response;

          this.isSaving = false;

          this.notificationService.success(
            response.str
          );

          // Clear password fields after successful update
          this.passwordForm.reset();

        },

        error: error => {

          this.isSaving = false;

          const message =
            error?.error?.message ??
            'Something went wrong while updating your password.';

          this.notificationService.error(
            message
          );

        }

      });

  }

}