
import { OrderStatus, PaymentStatus } from './order.enums';
import { OrderItemResponse } from './order-item-response';

export interface OrderResponse {
  id: string;
  userId: string;
  orderStatus: OrderStatus;
  orderTotal: number;
  currency: string;
  paymentTransactionId: string | null;
  paymentStatus: PaymentStatus | null;
  items: OrderItemResponse[];
  createdAt: string;
  updatedAt: string;
  completedAt: string | null;
}
