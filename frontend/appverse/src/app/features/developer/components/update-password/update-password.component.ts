import { Component } from '@angular/core';
import { MessageResponse } from '../../models/MessageResponse';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DeveloperService } from '../../services/developer.service';
import { NotificationService } from '../../../user/services/notificationService/NotificationService';
import { passwordMatchValidator } from '../../../shared/passwordMatchValidator';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-update-password',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './update-password.component.html',
  styleUrl: './update-password.component.scss'
})
export class UpdatePasswordComponent {
  response?: MessageResponse;
  passwordForm!: FormGroup;

  constructor(private fb: FormBuilder,private notificationService:NotificationService, private developerService: DeveloperService) {}

  ngOnInit(): void {
    this.passwordForm  = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', Validators.required],
      confirmPassword: ['', Validators.required]
    },{
      validators: passwordMatchValidator()
    })
  }

  updatePassword(){

    if (this.passwordForm?.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }


    const payload = this.passwordForm?.value;

    console.log("PAYLOAD SENT!");
    console.log(payload);

    this.developerService.updateDeveloperPassword(this.passwordForm?.value).subscribe({
      next: response =>{
        this.notificationService.success(response.str)
      },
      error: (error)=>{
            const message =
      error?.error?.message ??
      'Something went wrong';
        this.notificationService.error(message);
      }
    })
  }
}
