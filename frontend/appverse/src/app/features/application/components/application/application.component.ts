import {
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
import { MatMenuModule } from '@angular/material/menu';
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
    MatMenuModule
  ],

  templateUrl: './application.component.html',
  styleUrls: ['./application.component.scss']
})
export class ApplicationComponent implements OnInit {

  // IMPORTANT:
  // Makes the enum available inside the HTML template.
  readonly ApplicationStatus = ApplicationStatus;


  displayedColumns: string[] = [
    'name',
    'tagline',
    'status',
    'actions'
  ];


  dataSource =
    new MatTableDataSource<ApplicationResponse>();


  isLoading = false;

  searchTerm = '';


  @ViewChild(MatPaginator)
  paginator!: MatPaginator;


  @ViewChild(MatSort)
  sort!: MatSort;


  constructor(
    private applicationService: ApplicationService,
    private router: Router
  ) {}


  ngOnInit(): void {
    this.loadApplications();
  }


  loadApplications(): void {

    this.isLoading = true;

    this.applicationService
      .getMyApplications()
      .subscribe({

        next: (data) => {

          console.log('Applications:', data);

          this.dataSource.data = data;

          this.dataSource.paginator =
            this.paginator;

          this.dataSource.sort =
            this.sort;

          this.isLoading = false;
        },

        error: (err) => {

          console.error(
            'Failed to load applications',
            err
          );

          this.isLoading = false;
        }

      });

  }


  applyFilter(event: Event): void {

    const input =
      event.target as HTMLInputElement;

    this.searchTerm =
      input.value.trim().toLowerCase();

    this.dataSource.filter =
      this.searchTerm;

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


  deleteApplication(id: string): void {

    const confirmed = confirm(
      'Are you sure you want to delete this application? This action cannot be undone.'
    );

    if (!confirmed) {
      return;
    }

    this.applicationService
      .deleteApplication(id)
      .subscribe({

        next: () => {
          this.loadApplications();
        },

        error: (err) => {

          console.error(
            'Error deleting application:',
            err
          );

          alert(
            'Failed to delete application. Please try again.'
          );

        }

      });

  }


  updateApplicationStatus(
    app: ApplicationResponse,
    newStatus: ApplicationStatus
  ): void {

    const previousStatus =
      app.status;


    if (previousStatus === newStatus) {
      return;
    }


    const confirmed = confirm(
      `Are you sure you want to change "${app.name}" status to ${newStatus}?`
    );


    if (!confirmed) {
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

        error: (err) => {

          console.error(
            'Error updating application status:',
            err
          );

          alert(
            'Failed to update application status.'
          );

        }

      });

  }


  getStatusLabel(
    status:
      ApplicationStatus |
      string |
      null |
      undefined
  ): string {

    if (!status) {
      return 'Unknown';
    }


    switch (status) {

      case ApplicationStatus.PUBLISHED:
        return 'Published';

      case ApplicationStatus.UNPUBLISHED:
        return 'Unpublished';

      case ApplicationStatus.ARCHIVED:
        return 'Archived';

      case ApplicationStatus.DRAFT:
        return 'Draft';

      default:
        return status;

    }

  }


  getStatusIcon(
    status:
      ApplicationStatus |
      string |
      null |
      undefined
  ): string {

    switch (status) {

      case ApplicationStatus.PUBLISHED:
        return 'check_circle';

      case ApplicationStatus.UNPUBLISHED:
        return 'visibility_off';

      case ApplicationStatus.ARCHIVED:
        return 'inventory_2';

      case ApplicationStatus.DRAFT:
        return 'edit_note';

      default:
        return 'help_outline';

    }

  }

}