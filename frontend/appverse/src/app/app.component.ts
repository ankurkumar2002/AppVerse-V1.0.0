import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { getKeycloak } from './core/auth/keycloak';

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
  ) { }

  async ngOnInit(): Promise<void> {
    
  }
}
