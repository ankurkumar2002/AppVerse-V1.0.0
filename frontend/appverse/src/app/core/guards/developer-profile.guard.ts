import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { DeveloperService } from '../../features/developer/services/developer.service';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export const developerProfileGuard: CanActivateFn = () => {

  const developerService = inject(DeveloperService);
  const router = inject(Router);

  return developerService.getMyProfile().pipe(

    map(profile => {

      if (profile) {
        return router.createUrlTree(['/developer/dashboard']);
      }

      return true;
    }),

    catchError(error => {

      if (error?.status === 404) {
        return of(true);
      }

      console.error('Error checking developer profile:', error);

      return of(
        router.createUrlTree(['/landing'])
      );
    })
  );
};