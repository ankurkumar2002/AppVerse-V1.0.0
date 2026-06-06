import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export function passwordMatchValidator(): ValidatorFn{
    return (control: AbstractControl): ValidationErrors | null => {
        const newPassword = control.get('newPassword')?.value;
        const confirmPassword = control.get('confirmPassword')?.value;

        return newPassword === confirmPassword? null : {passwordMismatch: true};
    };
}