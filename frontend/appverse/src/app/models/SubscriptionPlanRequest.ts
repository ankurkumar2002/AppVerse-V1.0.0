import { SubscriptionPlanBillingInterval } from "./SubscriptionPlanBillingInterval";


export interface SubscriptionPlanRequest {
  name: string;                           // Required, max 150 chars
  description?: string;                   // Optional
  price: number;                          // Required, must be >= 0.00
  currency: string;                       // Required, 3-letter ISO code like "INR", "USD"
  billingInterval: SubscriptionPlanBillingInterval; // Required enum
  billingIntervalCount: number;          // Required, must be >= 1
  trialPeriodDays?: number;              // Optional, >= 0
  gatewayPlanPriceId?: string;           // Optional, payment gateway ID
  associatedApplicationIds: string[];    // Required, list of app IDs
}
