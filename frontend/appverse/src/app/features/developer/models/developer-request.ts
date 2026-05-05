export interface DeveloperRequest {
  name: string;
  email: string;
  website?: string;
  companyName?: string;
  bio?: string;
  logoUrl?: string;
  location?: string;
  developerType: 'INDIVIDUAL' | 'ORGANIZATION';
  role: 'USER' | 'DEVELOPER' | 'ADMIN';
}