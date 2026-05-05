import { Component, OnInit } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AsyncPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { DeveloperService } from '../../services/developer.service';
import { EditProfileDialogComponent } from '../edit-profile-dialog/edit-profile-dialog.component';
import { DeveloperRequest } from '../../models/developer-request';

@Component({
  selector: 'app-developer-dashboard',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    MatListModule,
    MatCardModule,
    MatProgressSpinnerModule,
    AsyncPipe,
    DatePipe
  ],
  templateUrl: './developer-dashboard.component.html',
  styleUrls: ['./developer-dashboard.component.scss']
})
export class DeveloperDashboardComponent implements OnInit {
  developer$ = this.developerService.profile$;
  isLoading = true;

  stats = [
    { icon: 'apps', value: 5, label: 'Published Apps' },
    { icon: 'people', value: 128, label: 'Active Users' },
    { icon: 'star', value: 4.8, label: 'Avg Rating' }
  ];

  recentActivities = [
    { message: "Updated profile settings", date: new Date() },
    { message: "Published new app", date: new Date(Date.now() - 86400000) }
  ];

  constructor(
    private dialog: MatDialog,
    private developerService: DeveloperService
  ) {}

  ngOnInit(): void {
    this.developerService.getMyDeveloperProfile().subscribe({
      next: () => this.isLoading = false,
      error: () => this.isLoading = false
    });
  }

  editProfile(): void {
    const currentProfile = this.developerService.getProfile();

    const dialogRef = this.dialog.open(EditProfileDialogComponent, {
      width: '500px',
      data: { ...currentProfile }
    });

    dialogRef.afterClosed().subscribe((result: DeveloperRequest) => {
      if (result) {
        this.isLoading = true;
        this.developerService.updateMyProfile(result).subscribe({
          next: () => this.isLoading = false,
          error: (err: any) => {
            console.error('Update failed', err);
            this.isLoading = false;
          }
        });
      }
    });
  }
}