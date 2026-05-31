import { Role } from "../../../models/enums/Role";
import { UserStatus } from "../../../models/enums/UserStatus";

export interface UserDetailsResponse{
    id: string;
    keycloakUserId: string;
    username: string;
    email: string;
    emailVerified: boolean;
    firstName: string;
    lastName: string;
    phone: string;
    role: Role;
    status: UserStatus;
    deactivatedByAdmin: boolean;
    createdAt: string;
    updatedAt: string | null;
    lastLoginAt: string | null;
}