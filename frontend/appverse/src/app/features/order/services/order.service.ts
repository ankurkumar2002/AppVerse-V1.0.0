
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { OrderResponse } from '../models/order-response';
import { CreateOrderRequest } from '../models/create-order-request';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private readonly apiUrl = 'http://localhost:9000/api/v1/orders';

  constructor(
    private http: HttpClient
  ) { }

  // GET /api/v1/orders/mine
  getMyOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(
      `${this.apiUrl}/mine`
    );
  }

  // GET /api/v1/orders/{orderId}
  getOrderById(orderId: string): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(
      `${this.apiUrl}/${encodeURIComponent(orderId)}`
    );
  }

  // POST /api/v1/orders
  createOrder(
    request: CreateOrderRequest
  ): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(
      this.apiUrl,
      request
    );
  }

  // POST /api/v1/orders/{orderId}/cancel
  cancelOrder(orderId: string): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(
      `${this.apiUrl}/${encodeURIComponent(orderId)}/cancel`,
      {}
    );
  }
}