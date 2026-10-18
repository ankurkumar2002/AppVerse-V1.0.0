
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { OrderResponse } from '../models/order-response';
import { CreateOrderRequest } from '../models/create-order-request';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private readonly apiUrl = '/api/orders';

  constructor(
    private http: HttpClient
  ) {}

  getMyOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(
      `${this.apiUrl}/my-orders`
    );
  }

  getOrderById(orderId: string): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(
      `${this.apiUrl}/${encodeURIComponent(orderId)}`
    );
  }

  createOrder(
    request: CreateOrderRequest
  ): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(
      this.apiUrl,
      request
    );
  }
}
