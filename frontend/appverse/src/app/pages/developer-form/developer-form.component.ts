import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DeveloperService } from '../../core/developer/developer.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { OAuthService } from 'angular-oauth2-oidc';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-developer-form',
    standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './developer-form.component.html',
})
export class DeveloperFormComponent {
  developerForm: FormGroup;

  constructor(
  private fb: FormBuilder,
  private keycloakService: KeycloakService,
  private developerService: DeveloperService,
  private router: Router
) {
  // const token = this.oauthService.getAccessToken();
  // const claims = this.oauthService.getIdentityClaims() as any;

  const keycloak = this.keycloakService.getKeycloakInstance();

  // Extend the type to include the expected properties
  interface KeycloakTokenParsed {
    given_name?: string;
    family_name?: string;
    email?: string;
    [key: string]: any;
  }

  const tokenParsed = keycloak.tokenParsed as KeycloakTokenParsed;

  const name = `${tokenParsed?.given_name ?? ''} ${tokenParsed?.family_name ?? ''}`.trim();
  const email = tokenParsed?.email ?? '';


    this.developerForm = this.fb.group({
    name: [{ value: name, disabled: true }],
    email: [{ value: email, disabled: true }],
    bio: ['', Validators.required],
    website: [''],
    companyName: [''],
    logoUrl: [''],
    location: [''],
    developerType: ['', Validators.required],
    role: ['DEVELOPER', Validators.required]
  });
  }

  onSubmit(): void {
  if (this.developerForm.valid) {
    const payload = this.developerForm.getRawValue(); // <-- THIS includes disabled fields
    this.developerService.createDeveloper(payload).subscribe({
      next: () => this.router.navigate(['/developer/dashboard']),
      error: (err) => console.error('Error creating developer:', err)
    });
  }
}

}
