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

    MatInputModule

  ],

  templateUrl: './application.component.html',

  styleUrls: ['./application.component.scss']
})
export class ApplicationComponent
  implements OnInit, AfterViewInit {


  /*
   * ==========================================================
   * TABLE
   * ==========================================================
   */

  displayedColumns: string[] = [

    'name',

    'tagline',

    'status',

    'actions'

  ];


  dataSource =
    new MatTableDataSource<ApplicationResponse>();



  /*
   * ==========================================================
   * STATE
   * ==========================================================
   */

  isLoading = false;

  searchTerm = '';



  /*
   * ==========================================================
   * ENUM
   *
   * This makes ApplicationStatus available inside HTML.
   *
   * Example:
   *
   * ApplicationStatus.PUBLISHED
   *
   * ==========================================================
   */

  readonly ApplicationStatus = ApplicationStatus;



  /*
   * ==========================================================
   * VIEW CHILDREN
   * ==========================================================
   */

  @ViewChild(MatPaginator)
  paginator!: MatPaginator;


  @ViewChild(MatSort)
  sort!: MatSort;



  /*
   * ==========================================================
   * CONSTRUCTOR
   * ==========================================================
   */

  constructor(

    private applicationService:
      ApplicationService,

    private router: Router

  ) {}



  /*
   * ==========================================================
   * ON INIT
   * ==========================================================
   */

  ngOnInit(): void {

    this.configureFilter();

  }



  /*
   * ==========================================================
   * AFTER VIEW INIT
   *
   * Paginator and Sort are available here.
   * ==========================================================
   */

  ngAfterViewInit(): void {

    this.dataSource.paginator =
      this.paginator;

    this.dataSource.sort =
      this.sort;

    this.loadApplications();

  }



  /*
   * ==========================================================
   * TABLE FILTER CONFIGURATION
   * ==========================================================
   *
   * Search will check:
   *
   * - application name
   * - tagline
   * - description
   * - status
   *
   * ==========================================================
   */

  private configureFilter(): void {

    this.dataSource.filterPredicate =
      (
        app: ApplicationResponse,
        filter: string
      ): boolean => {

        const search =
          filter.trim().toLowerCase();


        if (!search) {

          return true;

        }


        const name =
          app.name?.toLowerCase() ?? '';


        const tagline =
          app.tagline?.toLowerCase() ?? '';


        const description =
          app.description?.toLowerCase() ?? '';


        const status =
          app.status?.toString().toLowerCase() ?? '';


        return (

          name.includes(search) ||

          tagline.includes(search) ||

          description.includes(search) ||

          status.includes(search)

        );

      };

  }



  /*
   * ==========================================================
   * LOAD APPLICATIONS
   * ==========================================================
   */

  loadApplications(): void {

    this.isLoading = true;


    this.applicationService
      .getMyApplications()
      .subscribe({

        next: (data) => {

          console.log(
            'Applications:',
            data
          );


          this.dataSource.data = data;


          /*
           * Re-attach paginator and sort.
           * This is useful after refresh.
           */

          this.dataSource.paginator =
            this.paginator;


          this.dataSource.sort =
            this.sort;


          this.isLoading = false;

        },


        error: (err) => {

          console.error(
            'Failed to load applications:',
            err
          );


          this.isLoading = false;

        }

      });

  }



  /*
   * ==========================================================
   * SEARCH
   * ==========================================================
   */

  applyFilter(event: Event): void {

    const input =
      event.target as HTMLInputElement;


    this.searchTerm =
      input.value
        .trim()
        .toLowerCase();


    this.dataSource.filter =
      this.searchTerm;


    /*
     * Always return to page 1
     * after searching.
     */

    if (this.dataSource.paginator) {

      this.dataSource.paginator.firstPage();

    }

  }



  /*
   * ==========================================================
   * CLEAR SEARCH
   * ==========================================================
   */

  clearSearch(): void {

    this.searchTerm = '';

    this.dataSource.filter = '';


    if (this.dataSource.paginator) {

      this.dataSource.paginator.firstPage();

    }

  }



  /*
   * ==========================================================
   * CREATE APPLICATION
   * ==========================================================
   */

  openCreateDialog(): void {

    this.router.navigate([
      '/developer/apps/create'
    ]);

  }



  /*
   * ==========================================================
   * DELETE APPLICATION
   * ==========================================================
   */

  deleteApplication(
    id: string
  ): void {


    const confirmed =
      confirm(
        'Are you sure you want to delete this application? ' +
        'This action cannot be undone.'
      );


    if (!confirmed) {

      return;

    }


    this.isLoading = true;


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


          this.isLoading = false;


          alert(
            'Failed to delete application. Please try again.'
          );

        }

      });

  }



  /*
   * ==========================================================
   * UPDATE APPLICATION STATUS
   * ==========================================================
   */

  updateApplicationStatus(
    event: Event,
    app: ApplicationResponse
  ): void {


    const selectElement =
      event.target as HTMLSelectElement;


    const previousStatus =
      app.status;


    const newStatus =
      selectElement.value as ApplicationStatus;



    /*
     * Nothing changed.
     */

    if (
      previousStatus === newStatus
    ) {

      return;

    }



    /*
     * Confirmation
     */

    const confirmed =
      confirm(
        `Are you sure you want to change "${app.name}" ` +
        `status to ${this.getStatusLabel(newStatus)}?`
      );



    /*
     * User cancelled.
     */

    if (!confirmed) {

      selectElement.value =
        previousStatus;

      return;

    }



    /*
     * Call backend.
     */

    this.applicationService
      .updateAppStatus(
        app.id,
        newStatus
      )
      .subscribe({

        next: () => {

          /*
           * Update UI immediately.
           */

          app.status =
            newStatus;

        },


        error: (err) => {

          console.error(
            'Error updating application status:',
            err
          );


          alert(
            'Failed to update application status.'
          );


          /*
           * Restore old value.
           */

          selectElement.value =
            previousStatus;

        }

      });

  }



  /*
   * ==========================================================
   * STATUS LABEL
   * ==========================================================
   */

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



  /*
   * ==========================================================
   * STATUS ICON
   *
   * Kept here because you may want to use it later.
   * The current HTML does not need it.
   * ==========================================================
   */

  getStatusIcon(
    status:
      ApplicationStatus |
      string |
      null |
      undefined
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