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
  profileForm!: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private userAuth: UserAuthService,
    private router: Router
  ) {}

  async ngOnInit(): Promise<void> {
    const kcProfile = await getKeycloak().loadUserProfile();

    this.profileForm = this.fb.group({
      username: [kcProfile.username ?? ''],
      email: [kcProfile.email ?? ''],
      firstName: [kcProfile.firstName ?? ''],
      lastName: [kcProfile.lastName ?? ''],
      keycloakUserId: [kcProfile.id],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      role: ['USER']
    });
  }

  submitProfile(): void {
    if (this.profileForm.invalid) return;

    const payload: UserRequest = this.profileForm.value;

    this.userAuth.createUser(payload).subscribe(() => {
      this.router.navigate(['/user/dashboard']);
    });
  }
}
