import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { keycloak } from './auth/keycloak';

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
    private router: Router
  ) {}

  async ngOnInit(): Promise<void> {
    if (!keycloak.authenticated) {
      return;
    }

    const token = keycloak.tokenParsed as any;
    const roles: string[] = token?.realm_access?.roles ?? [];

    if (roles.includes('DEVELOPER')) {
      this.router.navigate(['/developer/dashboard']);
    }
  }
}
