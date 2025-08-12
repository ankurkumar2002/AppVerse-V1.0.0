import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'appverse';

  constructor(
    private keycloak: KeycloakService,
    private router: Router
  ) {}

  async ngOnInit() {
    const isLoggedIn = await this.keycloak.isLoggedIn();
    console.log('Logged in?', isLoggedIn);

    if (isLoggedIn) {
      const roles = this.keycloak.getUserRoles();
      console.log('User roles:', roles);

      if (roles.includes('developer')) {
        console.log('Redirecting to developer dashboard...');
        this.router.navigate(['/developer/dashboard']);
      }
    }
  }
}
