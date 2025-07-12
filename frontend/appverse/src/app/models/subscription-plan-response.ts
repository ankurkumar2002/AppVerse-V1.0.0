export interface SubscriptionPlanResponse {
  id: string;
  name: string;
  description?: string;
  price: number;
  currency: string;
  billingInterval: string;
  billingIntervalCount: number;
  trialPeriodDays?: number;
  status: string;
  applicationId: string;
  developerId: string;
  createdAt?: Date;
  updatedAt?: Date;
}