import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject, takeUntil } from 'rxjs';

import { DeveloperService } from '../../core/services/developer/developer.service';
import { DeveloperResponse } from '../../models/developer-response';
import { keycloak } from '../../auth/keycloak';

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
    MatTooltipModule
  ],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit, OnDestroy {
  @ViewChild('sidenav') sidenav!: MatSidenav;

  private destroy$ = new Subject<void>();
  developer: DeveloperResponse | null = null;
  isSidenavCollapsed = false;
  profileGradient = this.getDeterministicGradient('default');

  constructor(
    private developerService: DeveloperService,
    private cdRef: ChangeDetectorRef
  ) {}

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
        next: dev => {
          this.developer = dev;
          this.profileGradient = this.getDeterministicGradient(dev?.name ?? 'default');
          this.cdRef.detectChanges();
        }
      });
  }

  async logout(): Promise<void> {
    await keycloak.logout({
      redirectUri: `${window.location.origin}/landing`,
    });
  }

  private getDeterministicGradient(seed: string): string {
    const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#9A86E4', '#5A7E9A'];
    const hash = seed.split('').reduce((a, c) => a + c.charCodeAt(0), 0);
    return `linear-gradient(135deg, ${colors[hash % colors.length]} 0%, ${colors[(hash + 1) % colors.length]} 100%)`;
  }
}
