// src/app/models/application-response.ts

import { ApplicationStatus } from "./application-status";

export interface ApplicationResponse {
  id: string;
  name: string;
  tagline: string;
  description: string;
  version: string;
  categoryId: string;
  price: number; // BigDecimal -> number in TS
  currency: string;
  isFree: boolean;
  monetizationType: 'FREE' | 'PAID' | 'SUBSCRIPTION'; // Adjust based on your enum values
  associatedSubscriptionPlanIds: string[];
  platforms: string[];
  accessUrl: string;
  websiteUrl: string;
  supportUrl: string;
  thumbnailUrl: string;
  screenshots: ScreenshotResponse[];
  developerId: string;
  developerName: string;
  categoryName: string;
  tags: string[];
  status: ApplicationStatus;
  publishedAt: string; // Instant -> ISO string
  createdAt: string; // Instant -> ISO string
  updatedAt: string; // Instant -> ISO string
  averageRating: number;
  ratingCount: number;
}

export interface ScreenshotResponse {
  id: string;
  filename: string;
  url: string;
  order: number;
  caption?: string;
}
