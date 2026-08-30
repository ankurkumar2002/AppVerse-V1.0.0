import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';

import {
  UserAuthService,
  UserRequest
} from '../../services/user/user-auth.service';

import { getKeycloak } from '../../../../core/auth/keycloak';

@Component({
  selector: 'app-profile-completion',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './profile-completion.component.html',
  styleUrls: ['./profile-completion.component.scss']
})
export class ProfileCompletionComponent implements OnInit {

  profileForm: FormGroup;

  loading = false;

  constructor(
    private fb: FormBuilder,
    private userAuth: UserAuthService,
    private router: Router
  ) {

    this.profileForm = this.fb.group({

      phone: [
        '',
        [
          Validators.required,
          Validators.pattern(/^[0-9]{10}$/)
        ]
      ]

    });

  }


  ngOnInit(): void {
  }


  submitProfile(): void {

    if (this.profileForm.invalid) {

      this.profileForm.markAllAsTouched();

      return;
    }


    if (this.loading) {
      return;
    }


    this.loading = true;


    const payload: UserRequest = this.profileForm.value;


    console.log('PROFILE PAYLOAD:', payload);


    this.userAuth.createUser(payload).subscribe({

      next: async () => {

        try {

          const kc = getKeycloak();


          console.log(
            'Refreshing token after profile completion...'
          );


          /*
           * User profile now exists in backend.
           *
           * Refresh the Keycloak token so that the
           * gateway gets the latest roles/claims.
           */

          await kc.updateToken(-1);


          console.log(
            'NEW TOKEN:',
            kc.tokenParsed
          );


          console.log(
            'UPDATED ROLES:',
            kc.tokenParsed?.realm_access?.roles
          );


          await this.router.navigate([
            '/user/dashboard'
          ]);


        } catch (error) {

          console.error(
            'Token refresh failed after profile completion:',
            error
          );


          await this.router.navigate([
            '/landing'
          ]);

        } finally {

          this.loading = false;

        }

      },


      error: (err) => {

        this.loading = false;

        console.error(
          'Profile creation failed:',
          err
        );

      }

    });

  }

}