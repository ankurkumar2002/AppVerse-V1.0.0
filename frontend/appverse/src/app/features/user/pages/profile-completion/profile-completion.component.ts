import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserAuthService, UserRequest, UserResponse } from '../../../../core/services/user/user-auth.service';
import { KeycloakService } from 'keycloak-angular'; // If you’re using keycloak-angular
import { CommonModule } from '@angular/common';

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
  profileForm!: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private userAuth: UserAuthService,
    private keycloakService: KeycloakService,
    private router: Router
  ) { }

  async ngOnInit() {
    const kcProfile = await this.keycloakService.loadUserProfile();

    this.profileForm = this.fb.group({
      username: [kcProfile.username || ''],
      email: [kcProfile.email || ''],
      firstName: [kcProfile.firstName || ''],
      lastName: [kcProfile.lastName || ''],
      keycloakUserId: [kcProfile.id || ''],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      role: 'USER'

    });

  }

  submitProfile() {
    if (this.profileForm.invalid) return;

    this.loading = true;

    const userReq: UserRequest = this.profileForm.value;

    this.userAuth.createUser(userReq).subscribe({
      next: (res: UserResponse) => {
        this.loading = false;
        // Redirect to dashboard/home after profile completion
        this.router.navigate(['/user/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        console.error('Error creating user:', err);
      }
    });
  }
}
