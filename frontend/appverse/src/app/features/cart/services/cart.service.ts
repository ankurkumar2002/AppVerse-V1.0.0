import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { addToCartRequest } from '../models/addCartRequest';
import { UpdateCartItemQuantityRequest } from '../models/updateCartItemQuantityRequest';
import { Observable } from 'rxjs';
import { CartResponse } from '../models/CartResponse';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  baseUrl = "http://localhost:9000/api/v1/carts/";

  constructor(private http: HttpClient) { }

  addToCart(addToCartRequest: addToCartRequest){
    return this.http.post(this.baseUrl+"mine/items", addToCartRequest);
  }

  getCart(): Observable<CartResponse> {
    return this.http.get<CartResponse>(`${this.baseUrl}mine`)
  }

  updateCartItemQuantity(applicationId: string, updateCartItemQuantity: UpdateCartItemQuantityRequest){
    return this.http.put(`${this.baseUrl}mine/items/${applicationId}`, updateCartItemQuantity)
  }

  removeItemFromCart(applicationId: string){
    return this.http.delete(`${this.baseUrl}mine/items/${applicationId}`)
  }

  clearCart(){
    return this.http.delete(`${this.baseUrl}mine`)
  }
}
