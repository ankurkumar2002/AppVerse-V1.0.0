import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { developerProfileGuard } from './developer-profile.guard';

describe('developerProfileGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => developerProfileGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
