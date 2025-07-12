import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { ApplicationService } from '../../core/application/application.service';
import { ApplicationResponse } from '../../models/application-response';
import { ApplicationCreateDialogComponent } from '../application-create-dialog/application-create-dialog.component';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-application',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    RouterModule
  ],
  templateUrl: './application.component.html',
  styleUrls: ['./application.component.scss']
})
export class ApplicationComponent implements OnInit {
  displayedColumns: string[] = ['name', 'tagline', 'status', 'actions'];

  dataSource = new MatTableDataSource<ApplicationResponse>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private applicationService: ApplicationService,
    public dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadApplications();
  }

  loadApplications() {
    this.applicationService.getAllApplications().subscribe(data => {
      this.dataSource.data = data;
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ApplicationCreateDialogComponent, {
      width: '500px',
      data: {}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadApplications();
      }
    });
  }

  deleteApplication(id: string): void {
    if (confirm(`Are you sure you want to delete application with ID ${id}?`)) {
      this.applicationService.deleteApplication(id).subscribe({
        next: () => this.loadApplications(),
        error: (err) => {
          console.error('Error deleting application:', err);
          alert('Failed to delete application. Check console for details.');
        }
      });
    }
  }
}
