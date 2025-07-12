export interface DeveloperResponse {
  id: string;
  name: string;
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