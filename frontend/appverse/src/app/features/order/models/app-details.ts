
import { OrderItemType } from './order.enums';

export interface AppDetails {
  id: string;
  name: string;
  version: string;
  price: number;
  currency: string;
  monetizationType: string;
  isFree: boolean;
}
