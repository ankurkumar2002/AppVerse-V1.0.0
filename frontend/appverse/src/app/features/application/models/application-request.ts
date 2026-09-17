import { MonetizationType } from '../../../models/monetization-type';
import { DeveloperOfferedSubscriptionPlanDto } from './developer-offered-subscription-plan-dto';

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
  accessUrl: string;
  websiteUrl?: string;
  supportUrl?: string;
  tags?: string[];
}