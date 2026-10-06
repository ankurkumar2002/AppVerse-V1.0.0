import {
  AfterViewInit,
  Component,
  OnInit,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import { CommonModule } from '@angular/common';

import {
  MatTableModule,
  MatTableDataSource
} from '@angular/material/table';

import {
  MatPaginator,
  MatPaginatorModule
} from '@angular/material/paginator';

import {
  MatSort,
  MatSortModule
} from '@angular/material/sort';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { FormsModule } from '@angular/forms';
import {
  Router,
  RouterModule
} from '@angular/router';

import { ApplicationService } from '../../services/application.service';
import { ApplicationResponse } from '../../models/application-response';
import { ApplicationStatus } from '../../models/application-status';

@Component({
  selector: 'app-application',
  standalone: true,
  encapsulation: ViewEncapsulation.None,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressBarModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './application.component.html',
  styleUrls: ['./application.component.scss']
})
export class ApplicationComponent implements OnInit, AfterViewInit {

  displayedColumns: string[] = [
    'name',
    'tagline',
    'status',
    'actions'
  ];

  dataSource = new MatTableDataSource<ApplicationResponse>();

  isLoading = false;
  searchTerm = '';

  readonly ApplicationStatus = ApplicationStatus;

  isDeleteDialogOpen = false;
  isDeleting = false;
  applicationToDelete: ApplicationResponse | null = null;

  errorMessage = '';

  @ViewChild(MatPaginator)
  paginator!: MatPaginator;

  @ViewChild(MatSort)
  sort!: MatSort;

  constructor(
    private applicationService: ApplicationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.configureFilter();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    this.loadApplications();
  }

  private configureFilter(): void {
    this.dataSource.filterPredicate = (
      app: ApplicationResponse,
      filter: string
    ): boolean => {

      const search = filter.trim().toLowerCase();

      if (!search) {
        return true;
      }

      const name = app.name?.toLowerCase() ?? '';
      const tagline = app.tagline?.toLowerCase() ?? '';
      const description = app.description?.toLowerCase() ?? '';
      const status = app.status?.toString().toLowerCase() ?? '';

      return (
        name.includes(search) ||
        tagline.includes(search) ||
        description.includes(search) ||
        status.includes(search)
      );
    };
  }

  loadApplications(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.applicationService
      .getMyApplications()
      .subscribe({
        next: data => {
          this.dataSource.data = data;

          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;

          this.isLoading = false;
        },
        error: err => {
          console.error('Failed to load applications:', err);

          this.dataSource.data = [];
          this.isLoading = false;
          this.errorMessage =
            err?.error?.message ||
            'Failed to load your applications.';
        }
      });
  }

  applyFilter(event: Event): void {
    const input = event.target as HTMLInputElement;

    this.searchTerm = input.value
      .trim()
      .toLowerCase();

    this.dataSource.filter = this.searchTerm;

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.dataSource.filter = '';

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  openCreateDialog(): void {
    this.router.navigate([
      '/developer/apps/create'
    ]);
  }

  openDeleteDialog(app: ApplicationResponse): void {
    if (this.isDeleting) {
      return;
    }

    this.applicationToDelete = app;
    this.isDeleteDialogOpen = true;
    this.errorMessage = '';

    document.body.style.overflow = 'hidden';
  }

  closeDeleteDialog(): void {
    if (this.isDeleting) {
      return;
    }

    this.isDeleteDialogOpen = false;
    this.applicationToDelete = null;

    document.body.style.overflow = '';
  }

  confirmDeleteApplication(): void {
    if (!this.applicationToDelete || this.isDeleting) {
      return;
    }

    const applicationId = this.applicationToDelete.id;

    this.isDeleting = true;
    this.errorMessage = '';

    this.applicationService
      .deleteApplication(applicationId)
      .subscribe({
        next: () => {
          this.isDeleting = false;
          this.isDeleteDialogOpen = false;
          this.applicationToDelete = null;

          document.body.style.overflow = '';

          this.loadApplications();
        },

        error: err => {
          console.error(
            'Error deleting application:',
            err
          );

          this.isDeleting = false;

          this.errorMessage =
            err?.error?.message ||
            'Failed to delete the application. Please try again.';

          this.isDeleteDialogOpen = false;
          this.applicationToDelete = null;

          document.body.style.overflow = '';
        }
      });
  }

  updateApplicationStatus(
    event: Event,
    app: ApplicationResponse
  ): void {

    const selectElement =
      event.target as HTMLSelectElement;

    const previousStatus = app.status;

    const newStatus =
      selectElement.value as ApplicationStatus;

    if (previousStatus === newStatus) {
      return;
    }

    const confirmed = confirm(
      `Are you sure you want to change "${app.name}" status to ${this.getStatusLabel(newStatus)}?`
    );

    if (!confirmed) {
      selectElement.value = previousStatus;
      return;
    }

    this.applicationService
      .updateAppStatus(
        app.id,
        newStatus
      )
      .subscribe({
        next: () => {
          app.status = newStatus;
        },

        error: err => {
          console.error(
            'Error updating application status:',
            err
          );

          selectElement.value = previousStatus;

          this.errorMessage =
            err?.error?.message ||
            'Failed to update application status.';
        }
      });
  }

  getStatusLabel(
    status:
      | ApplicationStatus
      | string
      | null
      | undefined
  ): string {

    if (!status) {
      return 'Unknown';
    }

    switch (status) {
      case 'PUBLISHED':
        return 'Published';

      case 'UNPUBLISHED':
        return 'Unpublished';

      case 'ARCHIVED':
        return 'Archived';

      case 'DRAFT':
        return 'Draft';

      case 'REJECTED':
        return 'Rejected';

      default:
        return status;
    }
  }

  getStatusIcon(
    status:
      | ApplicationStatus
      | string
      | null
      | undefined
  ): string {

    switch (status) {
      case 'PUBLISHED':
        return 'check_circle';

      case 'UNPUBLISHED':
        return 'visibility_off';

      case 'ARCHIVED':
        return 'inventory_2';

      case 'DRAFT':
        return 'edit_note';

      case 'REJECTED':
        return 'cancel';

      default:
        return 'help_outline';
    }
  }
}