import { Component, NgModule } from '@angular/core';
import { CartResponse } from '../../models/CartResponse';
import { CartService } from '../../services/cart.service';
import { NgIf, NgForOf, CommonModule } from "../../../../../../node_modules/@angular/common/common_module.d-NEF7UaHr";
import { CartItemComponent } from '../cart-item/cart-item.component';
import { CartSummaryComponent } from "../cart-summary/cart-summary.component";

@Component({
  selector: 'app-cart-page',
  imports: [CartItemComponent, CommonModule, CartSummaryComponent],
  templateUrl: './cart-page.component.html',
  styleUrl: './cart-page.component.scss'
})
export class CartPageComponent {
  cart?: CartResponse

  constructor(private cartService: CartService){}


  ngOnInit(): void {

  }

  loadCart(): void {
    this.cartService.getCart().subscribe({
      next : (data) => {
        this.cart = data;
      },
      error: (err) =>{
        console.error(err);
      }
    });
  }

  removeItem(appId: string): void{
    this.cartService.removeItemFromCart(appId).subscribe({
      next: () => this.loadCart()
    })
  }

  get totalAmount(): number {
  return this.cart?.items.reduce(
    (sum, item) => sum + (item.unitPrice * item.quantity),
    0
  ) ?? 0;
}

}
