export interface ApplicationDetail {
  _id: string;
  name: string;
  tagline: string;
  description: string;
  version: string;
  categoryId: string;
  price: string;
  isFree: boolean;
  monetizationType: string;
  thumbnailUrl: string;
  screenshots: Screenshot[];
  developerId: string;
  websiteUrl: string;
}

export interface Screenshot {
  _id: string;
  imageUrl: string;
  caption?: string;
  order: number;
}

export interface UpdateApplicationRequest {
  // Fields the user can edit
  name: string;
  description: string;
  version: string;
  categoryId: string;
  // ... add any other fields from your form

  // Fields we must send back to satisfy the backend DTO
  thumbnailUrl: string;
  developerId: string;
  status: string;
  // The list of screenshots. We will fill this with our dummy data.
  screenshots: ScreenshotRequest[];
  
  // Add ALL other non-nullable fields from your backend DTO
  // For example:
  price: number;
  currency: string;
  isFree: boolean;
  platforms: string[];
  accessUrl: string;
  websiteUrl: string;
  supportUrl: string;
  tags: string[];
  monetizationType: string; // Assuming MonetizationType is a string enum
}

/**
 * This interface matches your backend's `ScreenshotRequest` DTO.
 * This is the object we will create with the "lie" (the empty imageUrl).
 */
export interface ScreenshotRequest {
  imageUrl: string; // The field we will fill with an empty string: ""
  caption: string;
  order: number;
}