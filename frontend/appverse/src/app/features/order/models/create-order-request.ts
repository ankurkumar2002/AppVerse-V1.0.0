
import { OrderItemTypeDto } from './order.enums';

export interface CreateOrderItemRequest {
  applicationId: string;
  quantity: number;
  itemType: OrderItemTypeDto;
}

export interface CreateOrderRequest {
  items: CreateOrderItemRequest[];
}
