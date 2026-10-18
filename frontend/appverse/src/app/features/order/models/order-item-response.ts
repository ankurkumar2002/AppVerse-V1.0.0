
import {
  FulfillmentStatus,
  OrderItemType
} from './order.enums';

export interface OrderItemResponse {
  id: string;
  applicationId: string;
  applicationName: string;
  applicationVersion: string | null;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  currency: string;
  itemType: OrderItemType;
  subscriptionPlanId: string | null;
  fulfillmentStatus: FulfillmentStatus;
  createdAt: string;
}
