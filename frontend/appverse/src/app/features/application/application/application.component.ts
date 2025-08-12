import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common'; // Import NgClass
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { ApplicationService } from '../../../core/services/application/application.service';
import { ApplicationResponse } from '../../../models/application-response';
import { Router, RouterModule } from '@angular/router';

// Imports for the new AdminLTE look
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-application',
  standalone: true,
  encapsulation: ViewEncapsulation.None,
  imports: [
    CommonModule,
    RouterModule,
    NgClass, // For the dynamic status badges
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    // Add these new modules for the improved look
    MatCardModule,
    MatProgressBarModule,
    MatTooltipModule,
  ],
  templateUrl: './application.component.html',
  styleUrls: ['./application.component.scss']
})
export class ApplicationComponent implements OnInit {
  displayedColumns: string[] = ['name', 'tagline', 'status', 'actions'];
  dataSource = new MatTableDataSource<ApplicationResponse>();
  isLoading = false; // Flag to control the loading progress bar

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private applicationService: ApplicationService,
    public dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadApplications();
  }

  loadApplications() {
    this.isLoading = true; // Show loading bar
    this.applicationService.getMyApplications().subscribe({
      next: (data) => {
        this.dataSource.data = data;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.isLoading = false; // Hide loading bar on success
      },
      error: (err) => {
        console.error('Failed to load applications', err);
        this.isLoading = false; // Hide loading bar on error
      }
    });
  }

  openCreateDialog(): void {
    this.router.navigate(['/apps/create']);
  }

  deleteApplication(id: string): void {
    if (confirm(`Are you sure you want to delete this application? This action cannot be undone.`)) {
      this.applicationService.deleteApplication(id).subscribe({
        next: () => this.loadApplications(),
        error: (err) => {
          console.error('Error deleting application:', err);
          alert('Failed to delete application. See console for details.');
        }
      });
    }
  }
}
