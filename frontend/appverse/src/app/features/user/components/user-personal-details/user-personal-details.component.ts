import { Component } from '@angular/core';
import { UserAuthService, UserResponse } from '../../services/user/user-auth.service';
import { UserDetailsResponse } from '../../models/UserDetailsResponse';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from "@angular/router";

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-personal-details.component.html',
  styleUrls: ['./user-personal-details.component.scss']
})
export class UserProfileComponent {

  response?: UserDetailsResponse;

  constructor(private userService: UserAuthService) { }

  ngOnInit(): void{
    this.loadUserDetails();
  }


  loadUserDetails() {
    this.userService.getUserDetailsByKeycloakId().subscribe(
      {
        next: data => {
          this.response = data;
          console.log("Here are the details: ",this.response);
        },
        error: (err) => console.error(`Error fetching details`, err)
      })
  }
}
