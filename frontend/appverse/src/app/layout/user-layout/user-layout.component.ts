import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

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
    MatButtonModule,
    MatTooltipModule
  ],

  templateUrl: './user-layout.component.html',
  styleUrls: ['./user-layout.component.scss']
})
export class UserLayoutComponent implements OnInit {

  // ============================================================
  // SIDEBAR
  // ============================================================

  /*
   * Keep the original property name because your
   * current user-layout.html uses:
   *
   * [class.collapsed]="isCollapsed"
   *
   * and:
   *
   * {{ isCollapsed ? 'chevron_right' : 'chevron_left' }}
   */
  isCollapsed = false;


  /*
   * This is also provided so that if we later use the
   * developer-style layout structure, it will work.
   */
  isSidenavCollapsed = false;


  // ============================================================
  // USER PROFILE
  // ============================================================

  user: {
    firstName?: string;
    lastName?: string;
    email?: string;
  } | null = null;


  // ============================================================
  // PROFILE GRADIENT
  // ============================================================

  profileGradient =
    'linear-gradient(135deg, #8b5cf6, #6d3fe8)';


  // ============================================================
  // CONSTRUCTOR
  // ============================================================

  constructor() {}


  // ============================================================
  // INITIALIZATION
  // ============================================================

  async ngOnInit(): Promise<void> {
    await this.loadUserProfile();
  }


  // ============================================================
  // LOAD USER PROFILE
  // ============================================================

  private async loadUserProfile(): Promise<void> {

    try {

      const profile =
        await getKeycloak().loadUserProfile();

      this.user = {
        firstName: profile.firstName ?? '',
        lastName: profile.lastName ?? '',
        email: profile.email ?? ''
      };

    } catch (error) {

      console.error(
        'Failed to load user profile:',
        error
      );

      this.user = {
        firstName: 'User',
        lastName: '',
        email: 'user@example.com'
      };
    }
  }


  // ============================================================
  // ORIGINAL USER SIDEBAR METHOD
  // ============================================================

  toggleSidebar(): void {

    this.isCollapsed =
      !this.isCollapsed;

    /*
     * Keep both properties synchronized.
     * This allows either the old user HTML or the
     * developer-style HTML to work.
     */
    this.isSidenavCollapsed =
      this.isCollapsed;
  }


  // ============================================================
  // DEVELOPER-STYLE SIDEBAR METHOD
  // ============================================================

  toggleSidenav(): void {

    this.isSidenavCollapsed =
      !this.isSidenavCollapsed;

    /*
     * Keep the original property synchronized.
     */
    this.isCollapsed =
      this.isSidenavCollapsed;
  }


  // ============================================================
  // LOGOUT
  // ============================================================

  async logout(): Promise<void> {

    try {

      await getKeycloak().logout({
        redirectUri:
          `${window.location.origin}/landing`
      });

    } catch (error) {

      console.error(
        'Logout failed:',
        error
      );

    }
  }
}