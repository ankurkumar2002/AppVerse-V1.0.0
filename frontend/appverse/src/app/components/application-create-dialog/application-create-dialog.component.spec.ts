import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationCreateDialogComponent } from './application-create-dialog.component';

describe('ApplicationCreateDialogComponent', () => {
  let component: ApplicationCreateDialogComponent;
  let fixture: ComponentFixture<ApplicationCreateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationCreateDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApplicationCreateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
