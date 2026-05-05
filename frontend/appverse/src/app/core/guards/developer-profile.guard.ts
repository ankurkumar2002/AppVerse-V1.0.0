import { Injectable } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { DeveloperService } from '../core/services/developer/developer.service';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export const developerProfileGuard: CanActivateFn = () => {
  const developerService = inject(DeveloperService);
  const router = inject(Router);

  return developerService.getMyProfile().pipe(
    map((profile) => {
      if (profile && profile.id) {
        // If developer profile exists, redirect to dashboard
        router.navigate(['/developer/dashboard']);
        return false;
      }else{
        router.navigate(['/developer/create']);
        return true;
      }
      // Allow access if profile doesn't exist
    }),
    catchError((err) => {
      // In case of error (e.g. 404), assume profile does not exist
      return of(true);
    })
  );
};
