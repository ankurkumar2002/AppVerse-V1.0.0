import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CartService } from '../../services/cart.service';
import { CartItemResponse } from '../../models/CartItemResponse';

@Component({
  selector: 'app-cart-item',
  imports: [],
  templateUrl: './cart-item.component.html',
  styleUrl: './cart-item.component.scss'
})
export class CartItemComponent {
  constructor(private cartService : CartService){}

  @Input()
  item!: CartItemResponse

  @Output()
  remove = new EventEmitter<string>();

  removeItem(){
    this.remove.emit(this.item.applicationId)
  }
}
