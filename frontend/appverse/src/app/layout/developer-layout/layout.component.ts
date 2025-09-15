import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { DeveloperService } from '../../core/services/developer/developer.service';
import { DeveloperResponse } from '../../models/developer-response';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
// **** ADD THIS IMPORT ****
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    // **** ADD THE MODULE HERE ****
    MatTooltipModule
  ],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit, OnDestroy {
  @ViewChild('sidenav') sidenav!: MatSidenav;

  private destroy$ = new Subject<void>();
  profileGradient: string;
  developer: DeveloperResponse | null = null;
  isSidenavCollapsed = false;

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

  toggleSidenav(): void {
    this.isSidenavCollapsed = !this.isSidenavCollapsed;
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
    const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#9A86E4', '#5A7E9A'];
    const hash = seed.split('').reduce((acc, char) => char.charCodeAt(0) + acc, 0);
    const color1 = colors[hash % colors.length];
    const color2 = colors[(hash + 1) % colors.length];
    return `linear-gradient(135deg, ${color1} 0%, ${color2} 100%)`;
  }

  logout(): void {
  localStorage.clear();
  sessionStorage.clear();
  this.keycloakService.logout('http://localhost:4200/landing');
}

}