import { Component } from '@angular/core';
import { ApplicationService } from '../../../application/services/application.service';
import { ApplicationDetail, Screenshot } from '../../../application/models/application-detail';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-app-detail',
  imports: [CommonModule, RouterModule, MatButtonModule, MatIconModule],
  templateUrl: './app-detail.component.html',
  styleUrl: './app-detail.component.scss'
})
export class AppDetailComponent {
  appId!: string;
  application!: ApplicationDetail;
  thumbnailBlobUrl: string | null = null;
  screenshotBlobUrls: { [key: string]: string } = {};

  constructor(private appService: ApplicationService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.appId = this.route.snapshot.paramMap.get('id')!;

    this.loadApplicationDetails();
  }

  loadApplicationDetails() {
    this.appService.getApplicationById(this.appId).subscribe(app => {
      this.application = app;

      if (app.thumbnailUrl) {
        const filename = this.extractFileName(this.application.thumbnailUrl);
        if (filename) {
          this.appService.getImageAsBlob('thumbnails', filename).subscribe({
            next: (blob) => {
              const objectURL = URL.createObjectURL(blob);
              this.thumbnailBlobUrl = objectURL;
            },
            error: (err) => console.error(`Error fetching thumbnail '${filename}':`, err)
          });
        }
      }

      if (app.screenshots?.length > 0) {
        app.screenshots.forEach((screenshot: Screenshot) => {
          if (screenshot.imageUrl) {
            const filename = this.extractFileName(screenshot.imageUrl);
            if (filename) {
              this.appService.getImageAsBlob('screenshots', filename).subscribe({
                next: (blob) => {
                  const objectURL = URL.createObjectURL(blob);
                  this.screenshotBlobUrls[screenshot._id] = objectURL;
                },
                error: (err) => console.error(`Error fetching screenshot '${filename}': `, err)
              })
            }
          }
        })
      }
    })
  }

  extractFileName(fullPath: string): string {
    if (!fullPath) {
      return '';
    }
    return fullPath.split('/').pop() || '';
  }

  loadThumbnail(filename: string) {

    this.appService
      .getImageAsBlob('thumbnails', filename)
      .subscribe({

        next: (blob) => {
          this.thumbnailBlobUrl = URL.createObjectURL(blob);
        },

        error: (err) => {
          console.error(err);
        }

      });

  }
}
