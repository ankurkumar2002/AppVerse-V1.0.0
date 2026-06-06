import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdatePersonelDetailsComponent } from './update-personel-details.component';

describe('UpdatePersonelDetailsComponent', () => {
  let component: UpdatePersonelDetailsComponent;
  let fixture: ComponentFixture<UpdatePersonelDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdatePersonelDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdatePersonelDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
