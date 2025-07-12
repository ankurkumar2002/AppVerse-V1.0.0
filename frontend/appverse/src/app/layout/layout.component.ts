import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { DeveloperService } from '../core/developer/developer.service';
import { DeveloperResponse } from '../models/developer-response';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    RouterModule,
    CommonModule
  ],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit {
  private destroy$ = new Subject<void>();
  profileGradient: string;
  developer: DeveloperResponse | null = null;

  constructor(
    private keycloakService: KeycloakService,
    private developerService: DeveloperService,
    private cdRef: ChangeDetectorRef
  ) {
    this.profileGradient = this.getDeterministicGradient('default');
  }

  ngOnInit(): void {
    this.loadDeveloperProfile();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDeveloperProfile(): void {
    this.developerService.getMyDeveloperProfile()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.developer = data;
          this.profileGradient = this.getDeterministicGradient(data?.name || 'default');
          this.cdRef.detectChanges();
        },
        error: (err) => console.error('Failed to load developer profile', err)
      });
  }

  getDeterministicGradient(seed: string): string {
    const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A'];
    const hash = seed.split('').reduce((acc, char) => char.charCodeAt(0) + acc, 0);
    const color = colors[hash % colors.length];
    return `linear-gradient(135deg, ${color} 0%, #1E1E1E 100%)`;
  }

  logout(): void {
    this.keycloakService.logout(window.location.origin);
  }
}