import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ApplicationService } from '../../services/application.service';
import { MatCardModule } from '@angular/material/card';
import { ApplicationDetail, Screenshot } from '../../models/application-detail';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'; 


@Component({
  selector: 'app-application-detail',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressSpinnerModule],
  templateUrl: './application-detail.component.html',
  styleUrls: ['./application-detail.component.scss']
})
export class ApplicationDetailComponent implements OnInit {
  appId!: string;
  application!: ApplicationDetail;
  thumbnailBlobUrl: SafeUrl | null = null;
  screenshotBlobUrls: { [key: string]: SafeUrl } = {};

  constructor(
    private route: ActivatedRoute,
    private applicationService: ApplicationService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.appId = this.route.snapshot.paramMap.get('id')!;
    if (this.appId) {
      this.loadApplicationDetails();
    }
  }

  loadApplicationDetails(): void {
    this.applicationService.getApplicationById(this.appId).subscribe(app => {
      this.application = app;

      // Load thumbnail
      if (app.thumbnailUrl) {
        const filename = this.extractFileName(app.thumbnailUrl);
        if (filename) {
          // Call the updated service method with the correct 'type'
          this.applicationService.getImageAsBlob('thumbnails', filename).subscribe({
            next: (blob) => {
              const objectURL = URL.createObjectURL(blob);
              this.thumbnailBlobUrl = this.sanitizer.bypassSecurityTrustUrl(objectURL);
            },
            error: (err) => console.error(`Error fetching thumbnail '${filename}':`, err)
          });
        }
      }

      // Load screenshots
      if (app.screenshots?.length > 0) {
        app.screenshots.forEach((screenshot: Screenshot) => {
          if (screenshot.imageUrl) {
            const filename = this.extractFileName(screenshot.imageUrl);
            if (filename) {
              // Call the updated service method with the correct 'type'
              this.applicationService.getImageAsBlob('screenshots', filename).subscribe({
                next: (blob) => {
                  const objectURL = URL.createObjectURL(blob);
                  this.screenshotBlobUrls[screenshot._id] = this.sanitizer.bypassSecurityTrustUrl(objectURL);
                },
                error: (err) => console.error(`Error fetching screenshot '${filename}':`, err)
              });
            }
          }
        });
      }
    });
  }

  extractFileName(fullPath: string): string {
    // This function is fine as is, it correctly gets the last part of the path.
    if (!fullPath) return '';
    return fullPath.split('/').pop() || '';
  }
}