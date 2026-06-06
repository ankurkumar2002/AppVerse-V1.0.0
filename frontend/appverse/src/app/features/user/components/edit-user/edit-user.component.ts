import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, ɵInternalFormsSharedModule } from '@angular/forms';
import { UserAuthService } from '../../services/user/user-auth.service';
import { UserDetailsResponse } from '../../models/UserDetailsResponse';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-edit-user',
  imports: [ɵInternalFormsSharedModule, ReactiveFormsModule, CommonModule],
  templateUrl: './edit-user.component.html',
  styleUrl: './edit-user.component.scss'
})
export class EditUserComponent {
  profileForm!: FormGroup;
  userDetails!: UserDetailsResponse;

  isLoading = false;
  isSaving = false;

  constructor(private fb: FormBuilder, private userService: UserAuthService) { }

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['']
    })
    this.loadProfile();
  }

  loadProfile(): void {
    this.userService.getUserDetailsByKeycloakId().subscribe({
      next: data => {
        this.userDetails = data;
        this.profileForm.patchValue(data);
      },
      error: (err) => console.log("Error Fetching details: ", err)
    })
  }

  updateProfile(): void {

    const payload = this.profileForm.value;

    console.log("PAYLOAD SENT:");
    console.log(payload);

    this.userService.updateUserProfile(
      payload
    ).subscribe({
      next: response => {
        console.log(response);
      }
    });
  }
}
