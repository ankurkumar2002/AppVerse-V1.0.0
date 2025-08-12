// src/app/models/application-request.ts

import { MonetizationType } from "./monetization-type";
import { SubscriptionPlanBillingInterval } from "./SubscriptionPlanBillingInterval";



export interface ApplicationRequest {
  id?: string;
  name: string;
  tagline: string;
  description: string;
  version: string;
  categoryId: string;
  price?: number; // Optional if app is free
  currency?: string;
  isFree: boolean;
  monetizationType: MonetizationType;
  offeredSubscriptionPlans?: DeveloperOfferedSubscriptionPlanDto[];
  platforms: string[]; // e.g., ['Web', 'Android']
  accessUrl?: string;
  websiteUrl?: string;
  supportUrl?: string;
  developerId: string;
  tags?: string[];
  status?: string;
  publishedAt?: string; // Use string for ISO format dates
}


// src/app/models/monetization-type.ts

// export enum MonetizationType {
//   FREE = 'FREE',
//   ONE_TIME_PURCHASE = 'ONE_TIME_PURCHASE',
//   SUBSCRIPTION = 'SUBSCRIPTION'
// }


// src/app/models/developer-offered-subscription-plan-dto.ts


export interface DeveloperOfferedSubscriptionPlanDto {
  planNameKey: string;             // e.g., "basic_plan"
  displayName: string;            // e.g., "Basic Plan"
  description?: string;
  price: number;                  // Should match BigDecimal on backend
  currency: string;               // 3-letter ISO currency code like "USD", "INR"
  billingInterval: SubscriptionPlanBillingInterval;
  billingIntervalCount: number;  // Must be >= 1
  trialPeriodDays?: number;      // Optional, >= 0
}
