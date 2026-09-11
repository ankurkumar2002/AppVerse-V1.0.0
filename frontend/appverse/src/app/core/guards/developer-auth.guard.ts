import { Injectable } from '@angular/core';
import {
  CanActivate,
  CanActivateChild,
  Router,
  UrlTree
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DeveloperService } from '../../features/developer/services/developer.service';

@Injectable({
  providedIn: 'root'
})
export class DeveloperAuthGuard implements CanActivate, CanActivateChild {

  constructor(
    private developerService: DeveloperService,
    private router: Router,
    private snackBar: MatSnackBar
  ) { }

  canActivate(): Observable<boolean | UrlTree> {
    return this.checkProfileAndRedirect();
  }

  canActivateChild(): Observable<boolean | UrlTree> {
    return this.checkProfileAndRedirect();
  }

  private checkProfileAndRedirect(): Observable<boolean | UrlTree> {

    return this.developerService.checkDeveloperProfileStatus().pipe(

      map(response => {

        if (response?.profileComplete === true) {
          return true;
        }

        this.showNotification(
          'Please complete your developer profile to continue.'
        );

        return this.router.createUrlTree([
          '/developer/create'
        ]);
      }),

      catchError(error => {

        console.error(
          'Error checking developer profile:',
          error
        );

        if (error?.status === 404) {

          this.showNotification(
            'Please create your developer profile to continue.'
          );

          return of(
            this.router.createUrlTree([
              '/developer/create'
            ])
          );
        }

        this.showNotification(
          'Unable to verify your developer profile. Please try again.'
        );

        return of(
          this.router.createUrlTree([
            '/landing'
          ])
        );
      })
    );
  }

  private showNotification(message: string): void {

    this.snackBar.open(
      message,
      'Close',
      {
        duration: 5000,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      }
    );
  }
}