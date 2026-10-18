
export type OrderStatus = string;

export type PaymentStatus = string;

export type FulfillmentStatus = string;

export type OrderItemType =
  | 'ONE_TIME_PURCHASE'
  | 'SUBSCRIPTION_INITIAL_PURCHASE';

export type OrderItemTypeDto =
  | 'ONE_TIME_PURCHASE'
  | 'SUBSCRIPTION_INITIAL_PURCHASE';
