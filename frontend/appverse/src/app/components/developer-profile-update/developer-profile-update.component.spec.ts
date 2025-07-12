import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeveloperProfileUpdateComponent } from './developer-profile-update.component';

describe('DeveloperProfileUpdateComponent', () => {
  let component: DeveloperProfileUpdateComponent;
  let fixture: ComponentFixture<DeveloperProfileUpdateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeveloperProfileUpdateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeveloperProfileUpdateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
