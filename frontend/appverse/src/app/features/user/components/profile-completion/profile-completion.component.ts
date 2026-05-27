import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { UserAuthService, UserRequest } from '../../../../core/services/user/user-auth.service';
import { getKeycloak } from '../../../../core/auth/keycloak';

@Component({
  selector: 'app-profile-completion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile-completion.component.html',
  styleUrls: ['./profile-completion.component.scss']
})
export class ProfileCompletionComponent implements OnInit {
  profileForm: FormGroup = this.fb.group({
  phone: [
    '',
    [
      Validators.required,
      Validators.pattern(/^[0-9]{10}$/)
    ]
  ]
});
  loading = false;

  constructor(
    private fb: FormBuilder,
    private userAuth: UserAuthService,
    private router: Router
  ) { }

  async ngOnInit(): Promise<void> {
    
  }

  submitProfile(): void {
    if (this.profileForm.invalid) return;

    const payload: UserRequest = this.profileForm.value;

    this.userAuth.createUser(payload).subscribe({

      next: async () => {

        try {

          const kc = getKeycloak();

          console.log('Refreshing token after profile completion...');

          await kc.updateToken(-1);

          console.log('NEW TOKEN:', kc.tokenParsed);

          console.log(
            'UPDATED ROLES:',
            kc.tokenParsed?.realm_access?.roles
          );

          await this.router.navigate(['/user/dashboard']);

        } catch (error) {

          console.error(
            'Token refresh failed after profile completion:',
            error
          );

          this.router.navigate(['/landing']);
        }
      },

      error: (err) => {

        console.error('Profile creation failed:', err);

      }

    });
  }
}
