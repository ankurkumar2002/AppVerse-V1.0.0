import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationRequest } from '../../../models/application-request';
import { ApplicationResponse } from '../../../models/application-response';
import { MessageResponse } from '../../../models/message-response';
import { ApplicationDetail, ScreenshotRequest, UpdateApplicationRequest } from '../../../models/application-detail';

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



deleteApplication(id: string): Observable < void> {
  return this.http.delete<void>(`${this.baseUrl}/${id}`);
}

// --- CORRECTED METHOD ---
getImageAsBlob(type: 'thumbnails' | 'screenshots', filename: string): Observable < Blob > {
  // We build the URL by adding '/images/...' to our existing baseUrl
  return this.http.get(`${this.baseUrl}/images/${type}/${filename}`, {
    responseType: 'blob'
  });
}
}