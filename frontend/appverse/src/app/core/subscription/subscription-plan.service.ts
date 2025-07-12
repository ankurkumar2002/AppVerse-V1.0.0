import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SubscriptionPlanResponse } from '../../models/subscription-plan-response';

export interface InternalPlanCreationRequest {
  planNameKey: string;
  displayName: string;
  description?: string;
  price: number;
  currency: string;
  billingInterval: string;
  billingIntervalCount: number;
  trialPeriodDays?: number;
  applicationId: string;
  developerId: string;
  gatewayPlanPriceId?: string;
}

@Injectable({ providedIn: 'root' })
export class SubscriptionPlanService {
  private readonly baseUrl = 'http://localhost:9000/api/v1/subscription-plans'; // Matches assumed SubscriptionPlanController

  constructor(private http: HttpClient) {}

  // POST create a new subscription plan (developer-specific)
  createDeveloperPlan(request: InternalPlanCreationRequest): Observable<SubscriptionPlanResponse> {
    console.log('Sending subscription plan creation request:', request);
    return this.http.post<SubscriptionPlanResponse>(`${this.baseUrl}/by-developer`, request);
  }

  // PUT update an existing subscription plan
  updatePlan(planId: string, request: InternalPlanCreationRequest): Observable<SubscriptionPlanResponse> {
    return this.http.put<SubscriptionPlanResponse>(`${this.baseUrl}/${planId}`, request);
  }

  // DELETE a subscription plan
  deletePlan(planId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${planId}`);
  }

  // GET a subscription plan by ID
  getPlanById(planId: string): Observable<SubscriptionPlanResponse> {
    return this.http.get<SubscriptionPlanResponse>(`${this.baseUrl}/${planId}`);
  }

  // GET all subscription plans
  getAllPlans(): Observable<SubscriptionPlanResponse[]> {
    return this.http.get<SubscriptionPlanResponse[]>(`${this.baseUrl}`);
  }
}