import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { getKeycloak } from '../../core/auth/keycloak';

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

  constructor(private router: Router) {}

  toggleSidebar(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  async logout(): Promise<void> {
    await getKeycloak().logout({
      redirectUri: `${window.location.origin}/landing`,
    });
  }
}
