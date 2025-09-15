import { Component } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakInstance } from 'keycloak-js';
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
  isDarkMode = true; // Default to dark mode for this theme
  private keycloakInstance: KeycloakInstance;

  constructor(private keycloakService: KeycloakService, private authService:AuthService) {
    this.keycloakInstance = this.keycloakService.getKeycloakInstance();
  }

  toggleTheme(): void {
    // Implement your theme toggling logic here, e.g., by adding/removing a class from the body
    this.isDarkMode = !this.isDarkMode;
    document.body.classList.toggle('light-theme', !this.isDarkMode);
  }

  login(role: 'user' | 'developer'): void {
    this.authService.login(role);
  }

  register(role: 'user' | 'developer'): void {
    this.authService.register(role);
  }
}