import { Injectable } from '@angular/core';
import {
  CanActivate,
  CanActivateChild,
  Router,
  UrlTree,
  RouterStateSnapshot,
  ActivatedRouteSnapshot
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DeveloperService } from '../core/services/developer/developer.service';

@Injectable({
  providedIn: 'root'
})
export class DeveloperAuthGuard implements CanActivate, CanActivateChild {

  constructor(
    private developerService: DeveloperService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    return this.handleRouteAccess(state.url);
  }

  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    return this.handleRouteAccess(state.url);
  }

  private handleRouteAccess(destinationUrl: string): Observable<boolean | UrlTree> {
    return this.developerService.checkDeveloperProfileStatus().pipe(
      map(response => response.profileComplete),
      catchError(() => of(false)),
      switchMap(isProfileComplete => this.checkAccess(isProfileComplete, destinationUrl))
    );
  }

  private checkAccess(isProfileComplete: boolean, destinationUrl: string): Observable<boolean | UrlTree> {
    const isCreateProfilePage = destinationUrl.includes('/developer/create');

    if (isProfileComplete) {
      if (isCreateProfilePage) {
        this.showNotification('You already have a developer profile.');
        return of(this.router.createUrlTree(['/developer/dashboard']));
      }
      return of(true);
    }

    if (!isProfileComplete) {
      if (isCreateProfilePage) {
        return of(true);
      }
      this.showNotification('Please complete your developer profile to continue.');
      return of(this.router.createUrlTree(['/developer/create']));
    }

    return of(false);
  }

  private showNotification(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
    });
  }
}
