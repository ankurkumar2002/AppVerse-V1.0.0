export interface DeveloperResponse {
  id: string;
  firstName: string;
lastName: string;
  email: string;
  website?: string;
  companyName?: string;
  bio?: string;
  logoUrl?: string;
  location?: string;
  developerType: 'INDIVIDUAL' | 'ORGANIZATION';
  role: 'USER' | 'DEVELOPER' | 'ADMIN';
  status: 'ACTIVE' | 'INACTIVE' | 'PENDING_VERIFICATION';
  isVerified: boolean;
  createdAt: string;
}