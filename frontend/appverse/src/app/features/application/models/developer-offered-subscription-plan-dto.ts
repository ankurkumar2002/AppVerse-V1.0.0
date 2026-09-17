import { SubscriptionPlanBillingInterval } from "../../../models/SubscriptionPlanBillingInterval";

export interface DeveloperOfferedSubscriptionPlanDto {
  planNameKey: string;
  displayName: string;
  description?: string;
  price: number;
  currency: string;
  billingInterval: SubscriptionPlanBillingInterval;
  billingIntervalCount: number;
  trialPeriodDays?: number;
}