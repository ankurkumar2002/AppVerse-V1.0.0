import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DeveloperService } from '../../../../core/services/developer/developer.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { keycloak } from '../../../../auth/keycloak';

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
    private developerService: DeveloperService,
    private http: HttpClient,
    private router: Router
  ) {
    const token = keycloak.tokenParsed as any;

    interface KeycloakTokenParsed {
      given_name?: string;
      family_name?: string;
      email?: string;
      sub?: string; // Keycloak user ID
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
    const role = sessionStorage.getItem('registeringAs'); 
    const keycloakUserId = keycloak.subject;

    // Assign role immediately on init
    if (role && keycloakUserId) {
      this.http.post('http://localhost:9000/api/v1/users/assign-role', {
        keycloakUserId,
        role: role.toUpperCase()
      }).subscribe({
        next: () => console.log(`Role ${role.toUpperCase()} assigned successfully`),
        error: err => console.error('Role assignment failed:', err)
      });
    }
  }

  onSubmit(): void {
    if (this.developerForm.valid) {
      const payload = this.developerForm.getRawValue();

     this.developerService.createDeveloper(payload).subscribe({
      next: async () => {
        try {
          await keycloak.login({
            redirectUri: `${window.location.origin}/developer/dashboard`,
          })
        } catch (err) {
          console.error('Error during logout:', err);
        }
      },
      error: err => {
        console.error('Error creating developer:', err);
      }
    });
    }
  }
}
