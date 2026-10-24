import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-cart-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cart-summary.component.html',
  styleUrl: './cart-summary.component.scss'
})
export class CartSummaryComponent {

  @Input() totalAmount = 0;
  @Input() isCheckingOut = false;

  @Output() checkout = new EventEmitter<void>();

  onProceedToCheckout(): void {
    if (this.totalAmount > 0 && !this.isCheckingOut) {
      this.checkout.emit();
    }
  }
}