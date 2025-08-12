import { Component } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatMenuModule } from "@angular/material/menu";

@Component({
  selector: 'app-landing',
  standalone: true,
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss'],
  imports: [MatToolbarModule, MatIconModule, MatButtonModule, MatMenuModule]
})
export class LandingComponent {
  isDarkMode = false;

  constructor(private keycloakService: KeycloakService) {}

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    const body = document.body;
    if (this.isDarkMode) {
      body.classList.add('dark-theme');
    } else {
      body.classList.remove('dark-theme');
    }
  }

  /**
   * Login as a specific role
   */
  login(role: 'user' | 'developer'): void {
    sessionStorage.setItem('registeringAs', role);
    this.keycloakService.login({
      redirectUri: `${window.location.origin}?mode=${role}`
    });
  }

  /**
   * Register as a specific role
   */
  register(role: 'user' | 'developer'): void {
    sessionStorage.setItem('registeringAs', role);
    this.keycloakService.login({
      redirectUri: `${window.location.origin}?mode=${role}&action=register`
    });
  }

  /**
   * These are optional helpers if you want
   * explicit register buttons without passing role param
   */
  registerAsDeveloper(): void {
    this.register('developer');
  }

  registerAsUser(): void {
    this.register('user');
  }
}
