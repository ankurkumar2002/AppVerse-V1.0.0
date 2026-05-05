// src/app/models/application-request.ts

import { MonetizationType } from "../../../models/monetization-type";
import { SubscriptionPlanBillingInterval } from "../../../models/SubscriptionPlanBillingInterval";



export interface ApplicationRequest {
  id?: string;
  name: string;
  tagline: string;
  description: string;
  version: string;
  categoryId: string;
  price?: number;
  currency?: string;
  isFree: boolean;
  monetizationType: MonetizationType;
  offeredSubscriptionPlans?: DeveloperOfferedSubscriptionPlanDto[];
  platforms: string[];
  accessUrl?: string;
  websiteUrl?: string;
  supportUrl?: string;
  tags?: string[];
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
