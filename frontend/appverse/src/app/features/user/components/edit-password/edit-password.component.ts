import { Component } from '@angular/core';
import { MessageResponse } from '../../../../models/message-response';
import { UserAuthService } from '../../services/user/user-auth.service';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators, ɵInternalFormsSharedModule } from '@angular/forms';
import { passwordMatchValidator } from '../../../shared/passwordMatchValidator';
import { NotificationService } from '../../services/notificationService/NotificationService';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-edit-password',
  imports: [ɵInternalFormsSharedModule,ReactiveFormsModule, CommonModule],
  templateUrl: './edit-password.component.html',
  styleUrl: './edit-password.component.scss'
})
export class EditPasswordComponent {
  response? : MessageResponse;
  passwordForm! : FormGroup;
  
  constructor(private notificationService: NotificationService,private fb: FormBuilder,private userService: UserAuthService){}

  ngOnInit(): void{
    this.passwordForm = this.fb.group({
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

    this.userService.updateUserPassword(this.passwordForm?.value).subscribe({
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
