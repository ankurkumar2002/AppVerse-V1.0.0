import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DeveloperService } from '../../../../core/services/developer/developer.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-developer-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './developer-form.component.html',
  styleUrls: ['./developer-form.component.scss']
})
export class DeveloperFormComponent implements OnInit {
  developerForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private keycloakService: KeycloakService,
    private developerService: DeveloperService,
    private http: HttpClient,
    private router: Router
  ) {
    const keycloak = this.keycloakService.getKeycloakInstance();

    interface KeycloakTokenParsed {
      given_name?: string;
      family_name?: string;
      email?: string;
      sub?: string; // <-- Keycloak user ID
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

  ngOnInit(): void {
    const role = sessionStorage.getItem('registeringAs'); // 'developer' or 'user'
    const keycloak = this.keycloakService.getKeycloakInstance();
    const keycloakUserId = keycloak.tokenParsed?.sub; // Keycloak ID

    if (role && keycloakUserId) {
      this.http.post('http://localhost:9000/api/v1/users/assign-role', {
        keycloakUserId,
        role: role.toUpperCase()
      }).subscribe({
        next: () => console.log('Role assigned'),
        error: err => console.error('Role assignment failed:', err)
      });
    }
  }

  onSubmit(): void {
    if (this.developerForm.valid) {
      const payload = this.developerForm.getRawValue();
      this.developerService.createDeveloper(payload).subscribe({
        next: () => this.router.navigate(['/developer/dashboard']),
        error: err => console.error('Error creating developer:', err)
      });
    }
  }
}
