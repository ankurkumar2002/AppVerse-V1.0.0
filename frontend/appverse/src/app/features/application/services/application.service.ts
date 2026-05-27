import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MessageResponse } from '../../../models/message-response';
import { ApplicationDetail, UpdateApplicationRequest, ScreenshotRequest } from '../models/application-detail';
import { ApplicationRequest } from '../models/application-request';
import { ApplicationResponse } from '../models/application-response';
import { ApplicationStatus } from '../models/application-status';
import { PageResponse } from '../../user/models/PageResponse';


@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  // This already has the prefix we need!
  private readonly baseUrl = 'http://localhost:9000/api/apps';

  constructor(private http: HttpClient) { }

  createApplication(
    request: ApplicationRequest,
    thumbnail?: File,
    screenshots?: File[],
    metadata?: { caption: string; order: number }[]
  ): Observable<MessageResponse> {
    const formData = new FormData();
    formData.append('request', JSON.stringify(request));

    if (thumbnail) {
      formData.append('thumbnail', thumbnail);
    }
    if (screenshots && screenshots.length > 0) {
      screenshots.forEach(file => formData.append('screenshots', file));
    }
    if (metadata && metadata.length > 0) {
      formData.append('metadata', JSON.stringify(metadata));
    }
    return this.http.post<MessageResponse>(this.baseUrl, formData);
  }

  getAllApplications(): Observable<ApplicationResponse[]> {
    return this.http.get<ApplicationResponse[]>(this.baseUrl);
  }

  getApplicationById(id: string): Observable<ApplicationDetail> {
    return this.http.get<ApplicationDetail>(`${this.baseUrl}/${id}`);
  }

  getMyApplications(): Observable<ApplicationResponse[]> {
    return this.http.get<ApplicationResponse[]>('http://localhost:9000/api/apps/my-apps');
  }

  getPublushedApplications(page: number, size: number): Observable<PageResponse<ApplicationResponse>>{
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<ApplicationResponse>>(`${this.baseUrl}/online`, {params})
  }


  updateApplication(
    id: string,
    request: UpdateApplicationRequest,
    thumbnail: File | null,
    screenshots: File[],
    metadata: ScreenshotRequest[]
  ): Observable<MessageResponse> {

    const formData = new FormData();

    // The backend now expects all JSON as simple strings
    formData.append('request', JSON.stringify(request));
    formData.append('metadata', JSON.stringify(metadata));

    if (thumbnail) {
      formData.append('thumbnail', thumbnail, thumbnail.name);
    }

    if (screenshots && screenshots.length > 0) {
      screenshots.forEach(file => {
        formData.append('screenshots', file, file.name);
      });
    }

    return this.http.put<MessageResponse>(`${this.baseUrl}/${id}`, formData);
  }



  deleteApplication(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // --- CORRECTED METHOD ---
  getImageAsBlob(type: 'thumbnails' | 'screenshots', filename: string): Observable<Blob> {
    // We build the URL by adding '/images/...' to our existing baseUrl
    return this.http.get(`${this.baseUrl}/images/${type}/${filename}`, {
      responseType: 'blob'
    });
  }

  updateAppStatus(id: string, status: ApplicationStatus): Observable<MessageResponse> {
    return this.http.patch<MessageResponse>(`${this.baseUrl}/${id}/status?status=${status}`, {});
  }

}