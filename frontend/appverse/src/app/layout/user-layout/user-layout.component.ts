import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-user-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './user-layout.component.html',
  styleUrls: ['./user-layout.component.scss']
})
export class UserLayoutComponent {
  isCollapsed = false;

  constructor(private keycloakService: KeycloakService, private router: Router) {}

  toggleSidebar() {
    this.isCollapsed = !this.isCollapsed;
  }

  async logout() {
    try {
      // Properly log out from Keycloak
      await this.keycloakService.logout(window.location.origin + '/landing');
    } catch (err) {
      console.error('Logout failed:', err);
      localStorage.clear();
      sessionStorage.clear();
      this.router.navigate(['/landing']);
    }
  }
}
