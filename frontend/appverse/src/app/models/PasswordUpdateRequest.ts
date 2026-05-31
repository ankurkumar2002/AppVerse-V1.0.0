export interface PasswordUpdateRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}