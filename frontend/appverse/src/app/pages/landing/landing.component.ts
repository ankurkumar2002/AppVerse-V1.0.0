import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { keycloak } from '../../auth/keycloak';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule
  ],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent {
  isDarkMode = true;

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    document.body.classList.toggle('light-theme', !this.isDarkMode);
  }

  // auth.service.ts
  login(role: 'user' | 'developer') {
    const redirectUri =
      role === 'developer'
        ? `${window.location.origin}/developer/dashboard`
        : `${window.location.origin}/user/dashboard`;

    keycloak.login({ redirectUri });
  }

  register(role: 'user' | 'developer') {
    const redirectUri =
      role === 'developer'
        ? `${window.location.origin}/developer/create`
        : `${window.location.origin}/user/profile-completion`;

    keycloak.login({
      action: 'register',
      redirectUri
    });
  }

}



