// src/app/core/developer/developer.service.ts

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';

import { tap, catchError } from 'rxjs/operators';
import { MessageResponse } from '../../../models/message-response';
import { DeveloperRequest } from '../models/developer-request';
import { DeveloperResponse } from '../models/developer-response';
import { ProfileUpdateRequest } from '../../../models/ProfileUpdateRequest';
import { PasswordUpdateRequest } from '../../../models/PasswordUpdateRequest';
import { updateDeveloperRequest } from '../models/update-developer-request';

@Injectable({ providedIn: 'root' })
export class DeveloperService {
  private readonly baseUrl = 'http://localhost:9000/api/developers';
  private profileSubject = new BehaviorSubject<DeveloperResponse | null>(null);
  public profile$ = this.profileSubject.asObservable();

  constructor(private http: HttpClient) {
    this.getMyDeveloperProfile().subscribe(); // preload profile
  }

  /** Create new developer */
  createDeveloper(data: DeveloperRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.baseUrl, data);
  }

  /** Fetch current developer */
  getMyDeveloperProfile(): Observable<DeveloperResponse> {
    return this.http.get<DeveloperResponse>(`${this.baseUrl}/me`).pipe(
      tap((profile) => this.profileSubject.next(profile))
    );
  }

  /** Get cached profile */
  getProfile(): DeveloperResponse | null {
    return this.profileSubject.getValue();
  }

  /** Update developer profile (DB + Keycloak behind the scenes) */
  updateMyProfile(updated: DeveloperRequest): Observable<MessageResponse> {
    const id = this.getProfile()?.id;
    if (!id) throw new Error('Developer ID not found for update.');
    return this.http.put<MessageResponse>(`${this.baseUrl}/${id}`, updated).pipe(
      tap(() => this.getMyDeveloperProfile().subscribe()), // Refresh profile cache
      catchError((error) => {
        console.error('Error updating developer profile:', error);
        throw error;
      })
    );
  }

  /** List all developers */
  getAllDevelopers(): Observable<DeveloperResponse[]> {
    return this.http.get<DeveloperResponse[]>(this.baseUrl);
  }

  /** Delete developer */
  deleteDeveloper(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

   checkDeveloperProfileStatus(): Observable<{ profileComplete: boolean }> {
    return this.http.get<{ profileComplete: boolean }>(`${this.baseUrl}/is-profile-complete`).pipe(
      catchError(error => {
        if (error.status === 404) {
          return of({ profileComplete: false });
        }
        throw error;
      })
    );
  }

  /** Developer existence check */
  checkIfDeveloperExists(id: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/exists?id=${id}`);
  }

  /** Check by Keycloak ID */
  checkExistsByKeycloakId(userId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/exists/by-keycloak-id/${userId}`);
  }

  /** Raw fetch */
  getMyProfile(): Observable<updateDeveloperRequest> {
    return this.http.get<updateDeveloperRequest>(`${this.baseUrl}/me`);
  }

  updateDeveloperProfile( profile: updateDeveloperRequest): Observable<MessageResponse> {
    return this.http.patch<MessageResponse>(`${this.baseUrl}/updateprofile`, profile);
  }

  updateDeveloperPassword(request: PasswordUpdateRequest): Observable<MessageResponse> {
    return this.http.put<MessageResponse>(`${this.baseUrl}/updatepassword`, request);
  }
  
}
