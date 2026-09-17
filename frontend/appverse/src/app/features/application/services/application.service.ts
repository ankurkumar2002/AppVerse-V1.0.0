import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MessageResponse } from '../../../models/message-response';
import {
  ApplicationDetail,
  UpdateApplicationRequest,
  ScreenshotRequest
} from '../models/application-detail';
import { ApplicationRequest } from '../models/application-request';
import { ApplicationResponse } from '../models/application-response';
import { ApplicationStatus } from '../models/application-status';
import { PageResponse } from '../../user/models/PageResponse';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {

  private readonly baseUrl =
    'http://localhost:9000/api/apps';

  constructor(
    private http: HttpClient
  ) { }

  createApplication(
    request: ApplicationRequest,
    thumbnail?: File,
    screenshots?: File[],
    metadata?: { caption: string; order: number }[]
  ): Observable<MessageResponse> {

    const formData =
      new FormData();

    formData.append(
      'request',
      JSON.stringify(request)
    );

    if (thumbnail) {
      formData.append(
        'thumbnail',
        thumbnail,
        thumbnail.name
      );
    }

    if (
      screenshots &&
      screenshots.length > 0
    ) {
      screenshots.forEach(file => {
        formData.append(
          'screenshots',
          file,
          file.name
        );
      });
    }

    if (
      metadata &&
      metadata.length > 0
    ) {
      formData.append(
        'metadata',
        JSON.stringify(metadata)
      );
    }

    return this.http.post<MessageResponse>(
      this.baseUrl,
      formData
    );
  }

  getAllApplications(): Observable<ApplicationResponse[]> {

    return this.http.get<ApplicationResponse[]>(
      this.baseUrl
    );
  }

  getApplicationById(
    id: string
  ): Observable<ApplicationDetail> {

    return this.http.get<ApplicationDetail>(
      `${this.baseUrl}/${id}`
    );
  }

  getPublishedApplicationById(
    id: string
  ): Observable<ApplicationResponse> {

    return this.http.get<ApplicationResponse>(
      `${this.baseUrl}/published/${id}`
    );
  }

  getMyApplications(): Observable<ApplicationResponse[]> {

    return this.http.get<ApplicationResponse[]>(
      `${this.baseUrl}/my-apps`
    );
  }

  getPublushedApplications(
    page: number,
    size: number
  ): Observable<PageResponse<ApplicationResponse>> {

    const params =
      new HttpParams()
        .set(
          'page',
          page.toString()
        )
        .set(
          'size',
          size.toString()
        );

    return this.http.get<PageResponse<ApplicationResponse>>(
      `${this.baseUrl}/online`,
      { params }
    );
  }

  updateApplication(
    id: string,
    request: UpdateApplicationRequest,
    thumbnail: File | null,
    screenshots: File[],
    metadata: ScreenshotRequest[]
  ): Observable<MessageResponse> {

    const formData =
      new FormData();

    formData.append(
      'request',
      JSON.stringify(request)
    );

    formData.append(
      'metadata',
      JSON.stringify(metadata)
    );

    if (thumbnail) {
      formData.append(
        'thumbnail',
        thumbnail,
        thumbnail.name
      );
    }

    if (
      screenshots &&
      screenshots.length > 0
    ) {
      screenshots.forEach(file => {
        formData.append(
          'screenshots',
          file,
          file.name
        );
      });
    }

    return this.http.put<MessageResponse>(
      `${this.baseUrl}/${id}`,
      formData
    );
  }

  deleteApplication(
    id: string
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.baseUrl}/${id}`
    );
  }

  getImageAsBlob(
    type: 'thumbnails' | 'screenshots',
    filename: string
  ): Observable<Blob> {

    const normalizedFilename =
      this.extractFilename(filename);

    return this.http.get(
      `${this.baseUrl}/images/${type}/${encodeURIComponent(normalizedFilename)}`,
      {
        responseType: 'blob'
      }
    );
  }

  private extractFilename(
    value: string
  ): string {

    if (!value) {
      return '';
    }

    let filename =
      value
        .trim()
        .split('?')[0]
        .split('#')[0]
        .replace(/\\/g, '/');

    const lastSlash =
      filename.lastIndexOf('/');

    if (lastSlash >= 0) {
      filename =
        filename.substring(
          lastSlash + 1
        );
    }

    try {
      filename =
        decodeURIComponent(filename);
    } catch {
    }

    return filename;
  }

  updateAppStatus(
    id: string,
    status: ApplicationStatus
  ): Observable<MessageResponse> {

    return this.http.patch<MessageResponse>(
      `${this.baseUrl}/${id}/status?status=${status}`,
      {}
    );
  }
}