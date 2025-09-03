import { Component } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakInstance } from 'keycloak-js'; // Import the type for clarity
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatMenuModule } from "@angular/material/menu";
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule, 
    MatIconModule, 
    MatButtonModule, 
    MatMenuModule
  ]
})
export class LandingComponent {
  isDarkMode = false;
  private keycloakInstance: KeycloakInstance;

  constructor(private keycloakService: KeycloakService, private authService:AuthService) {
    // Get the instance once in the constructor
    this.keycloakInstance = this.keycloakService.getKeycloakInstance();
  }

  toggleTheme(): void {
    // ... your theme logic
  }

  /**
   * Login with a specific UI based on the role.
   * This function does NOT need to be async.
   */
  login(role: 'user' | 'developer'): void {
    // This now correctly calls your powerful service
    this.authService.login(role);
  }

  register(role: 'user' | 'developer'): void {
    // This now correctly calls your powerful service
    this.authService.register(role);
  }
}