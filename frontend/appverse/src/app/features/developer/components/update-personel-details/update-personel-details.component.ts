import { Component } from '@angular/core';
import { updateDeveloperRequest } from '../../models/update-developer-request';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DeveloperService } from '../../services/developer.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-update-personel-details',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './update-personel-details.component.html',
  styleUrl: './update-personel-details.component.scss'
})
export class UpdatePersonelDetailsComponent {
  updateDeveloperRequest!: updateDeveloperRequest;
  developerProfileForm!: FormGroup;

  isLoading = false;
  isSaving = false;

  constructor(private fb: FormBuilder, private developerService: DeveloperService) {}


  ngOnInit(): void {
    this.developerProfileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]]
    })
    this.loadDeveloperProfile();
  }

  loadDeveloperProfile(): void {
    this.developerService.getMyDeveloperProfile().subscribe({
      next: data =>{
        this.updateDeveloperRequest = data;
        this.developerProfileForm.patchValue(data);
      },
      error: (err) =>console.log("Error Fetching details: ", err)
    }
    )
  }

  updateProfile(): void {
    const payload = this.developerProfileForm.value;

    console.log("PAYLOAD SENT:");
    console.log(payload);

    this.developerService.updateDeveloperProfile(payload).subscribe({
      next: response => {
        console.log(response);
      }
    })
  }
}
