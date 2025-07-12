import { Injectable } from '@angular/core';
import {
  CanActivate,
  Router,
  UrlTree
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { DeveloperService } from '../core/developer/developer.service';

@Injectable({
  providedIn: 'root'
})
export class DeveloperAuthGuard implements CanActivate {

  constructor(
    private developerService: DeveloperService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.developerService.checkDeveloperProfileStatus().pipe(
      map((res) => {
        if (res.profileComplete === true) {
          return true;
        } else {
          return this.router.createUrlTree(['/developer/create']);
        }
      }),
      catchError((err) => {
        // If error (like 401, 500), redirect or deny access
        return of(this.router.createUrlTree(['/developer/create']));
      })
    );
  }
}
