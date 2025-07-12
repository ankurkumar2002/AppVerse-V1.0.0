import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { developerAuthGuard } from './developer-auth.guard';

describe('developerAuthGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => developerAuthGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
