import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserRequest {
  phone: string;
}

export interface UserResponse {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  keycloakUserId: string;
  role: string;
  status: string;
}

export interface RoleAssignRequest {
  keycloakUserId: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserAuthService {
  private readonly baseUrl = 'http://localhost:9000/api/v1/users'; 

  constructor(private http: HttpClient) {}

  

  createUser(user: UserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.baseUrl}`, user);
  }

  getUserById(userId: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/${userId}`);
  }

  getUserByKeycloakId(keycloakUserId: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/keycloak/${keycloakUserId}`);
  }

  getUserByUsername(username: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/username/${username}`);
  }

  getUserByEmail(email: string): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/email/${email}`);
  }

  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.baseUrl}`);
  }

  updateUserProfile(userId: string, user: UserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.baseUrl}/${userId}`, user);
  }

  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${userId}`);
  }

  // --- Extra Endpoints ---

  recordUserLogin(userId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${userId}/record-login`, {});
  }

  getMyProfile(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/me`);
  }

  assignRoleToUser(request: RoleAssignRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/assign-role`, request);
  }
}
