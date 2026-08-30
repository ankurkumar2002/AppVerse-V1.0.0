import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { DeveloperService } from '../../services/developer.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { getKeycloak } from '../../../../core/auth/keycloak';

@Component({
  selector: 'app-developer-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './developer-form.component.html',
  styleUrls: ['./developer-form.component.scss']
})
export class DeveloperFormComponent implements OnInit {

  developerForm: FormGroup;

  isSubmitting = false;


  constructor(
    private fb: FormBuilder,
    private developerService: DeveloperService,
    private http: HttpClient,
    private router: Router
  ) {

    interface KeycloakTokenParsed {
      given_name?: string;
      family_name?: string;
      email?: string;
      sub?: string;
      [key: string]: any;
    }

    const tokenParsed =
      getKeycloak().tokenParsed as KeycloakTokenParsed;


    const name =
      `${tokenParsed?.given_name ?? ''} ${tokenParsed?.family_name ?? ''}`
        .trim();


    const email =
      tokenParsed?.email ?? '';


    this.developerForm = this.fb.group({

      name: [
        {
          value: name,
          disabled: true
        }
      ],

      email: [
        {
          value: email,
          disabled: true
        }
      ],

      bio: [
        '',
        Validators.required
      ],

      website: [
        ''
      ],

      companyName: [
        ''
      ],

      logoUrl: [
        ''
      ],

      location: [
        ''
      ],

      developerType: [
        '',
        Validators.required
      ],

      role: [
        'DEVELOPER',
        Validators.required
      ]

    });

  }


  ngOnInit(): void {

    const role =
      sessionStorage.getItem('registeringAs');

    const keycloakUserId =
      getKeycloak().subject;


    /*
     * Assign developer role immediately.
     */
    if (role && keycloakUserId) {

      this.http
        .post(
          'http://localhost:9000/api/v1/users/assign-role',
          {
            keycloakUserId,
            role: role.toUpperCase()
          }
        )
        .subscribe({

          next: () => {

            console.log(
              `Role ${role.toUpperCase()} assigned successfully`
            );

          },

          error: err => {

            console.error(
              'Role assignment failed:',
              err
            );

          }

        });

    }

  }


  onSubmit(): void {

    if (this.developerForm.invalid) {

      this.developerForm.markAllAsTouched();

      return;

    }


    if (this.isSubmitting) {
      return;
    }


    this.isSubmitting = true;


    /*
     * IMPORTANT:
     *
     * getRawValue() is required because
     * name and email are disabled controls.
     *
     * form.value would NOT include disabled
     * controls.
     */
    const payload =
      this.developerForm.getRawValue();


    console.log(
      'DEVELOPER REGISTRATION PAYLOAD:',
      payload
    );


    this.developerService
      .createDeveloper(payload)
      .subscribe({

        next: async () => {

          try {

            console.log(
              'Developer profile created successfully.'
            );


            /*
             * Re-login so Keycloak gets
             * a fresh token containing the
             * newly assigned role.
             */
            await getKeycloak().login({

              redirectUri:
                `${window.location.origin}/developer/dashboard`

            });

          } catch (err) {

            console.error(
              'Error during Keycloak login:',
              err
            );

            this.isSubmitting = false;

          }

        },


        error: err => {

          console.error(
            'Error creating developer:',
            err
          );

          this.isSubmitting = false;

        }

      });

  }

}