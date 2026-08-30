import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

import {
  UserAuthService
} from '../../services/user/user-auth.service';

import {
  UserDetailsResponse
} from '../../models/UserDetailsResponse';


@Component({
  selector: 'app-user-profile',

  standalone: true,

  imports: [
    CommonModule,
    DatePipe,
    RouterLink,
    MatIconModule
  ],

  templateUrl: './user-personal-details.component.html',

  styleUrls: ['./user-personal-details.component.scss']
})
export class UserProfileComponent implements OnInit {

  response?: UserDetailsResponse;


  constructor(
    private userService: UserAuthService
  ) {}


  ngOnInit(): void {

    this.loadUserDetails();

  }


  loadUserDetails(): void {

    this.userService
      .getUserDetailsByKeycloakId()
      .subscribe({

        next: (data) => {

          this.response = data;

          console.log(
            'Here are the user details:',
            this.response
          );

        },

        error: (err) => {

          console.error(
            'Error fetching user details:',
            err
          );

        }

      });

  }

}