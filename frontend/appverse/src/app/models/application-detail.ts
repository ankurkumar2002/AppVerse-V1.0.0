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