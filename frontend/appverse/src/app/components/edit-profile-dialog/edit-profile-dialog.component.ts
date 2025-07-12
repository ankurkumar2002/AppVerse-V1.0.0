import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { DeveloperResponse } from '../../models/developer-response';


@Component({
  imports: [
    MatInputModule,
    MatFormFieldModule,
    FormsModule,
    MatButtonModule,
    MatDialogModule
  ],
  template: `
    <h2 mat-dialog-title>Edit Profile</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline">
        <mat-label>Name</mat-label>
        <input matInput [(ngModel)]="data.name" required>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Bio</mat-label>
        <textarea matInput [(ngModel)]="data.bio" rows="4"></textarea>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Website</mat-label>
        <input matInput [(ngModel)]="data.website" placeholder="https://">
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" (click)="onSave()" 
              [disabled]="!data.name">Save</button>
    </mat-dialog-actions>
  `,
  styles: [`
    mat-dialog-content {
      display: flex;
      flex-direction: column;
      gap: 16px;
      padding: 16px 0;
    }
    mat-form-field {
      width: 100%;
    }
  `]
})
export class EditProfileDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<EditProfileDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DeveloperResponse
  ) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.data.name) { // Basic validation
      this.dialogRef.close(this.data);
    }
  }
}