import { Component, inject, signal, model, ElementRef, ViewChild, computed } from '@angular/core';
import { CommonModule, NgOptimizedImage } from '@angular/common';
import { ImageUploadService } from '../../services/image-upload.service';

@Component({
  standalone: true,
  imports: [CommonModule],
  template: `
    <div 
      class="upload-zone" 
      [class.drag-over]="isDragOver()"
      [class.has-image]="displayUrl()"
      (click)="triggerFileInput()"
    >
      <input 
        #fileInput
        type="file" 
        accept="image/*" 
        class="hidden-input" 
        (change)="onFileSelected($event)" 
      />

      @if (isUploading()) {
        <div class="loading-state">
          <div class="spinner"></div>
          <p>Uploading...</p>
        </div>
      } @else if (displayUrl()) {
        <div class="preview-container">
          <!-- We use displayUrl() to map internal holocron:// URIs to actual /api/images endpoints. -->
          <img [src]="displayUrl()" alt="Uploaded Image" class="preview-image" />
          <button class="remove-btn" (click)="removeImage($event)">
            <span class="material-symbols-outlined">delete</span>
          </button>
        </div>
      } @else {
        <div class="empty-state">
          <span class="material-symbols-outlined upload-icon">add_photo_alternate</span>
          <p class="primary-text">Click or drag image to upload</p>
          <p class="secondary-text">PNG, JPG, GIF up to 10MB</p>
        </div>
      }
    </div>
  `,
  styles: [`
    .upload-zone {
      border: 2px dashed var(--surface-border, #334155);
      border-radius: 8px;
      padding: 2rem;
      text-align: center;
      cursor: pointer;
      transition: all 0.2s ease;
      background: var(--surface-bg, #1e293b);
      position: relative;
      overflow: hidden;
      min-height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .upload-zone:hover, .drag-over {
      border-color: var(--primary-color, #6366f1);
      background: var(--surface-hover-bg, #283548);
    }

    .upload-zone.has-image {
      border-style: solid;
      padding: 0;
    }

    .hidden-input {
      display: none;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
    }

    .upload-icon {
      font-size: 3rem;
      color: var(--text-muted, #94a3b8);
      margin-bottom: 0.5rem;
    }

    .primary-text {
      font-weight: 500;
      color: var(--text-color, #f8fafc);
      margin: 0;
    }

    .secondary-text {
      font-size: 0.875rem;
      color: var(--text-muted, #94a3b8);
      margin: 0;
    }

    .preview-container {
      width: 100%;
      height: 100%;
      position: relative;
    }

    .preview-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    .remove-btn {
      position: absolute;
      top: 0.5rem;
      right: 0.5rem;
      background: rgba(0, 0, 0, 0.6);
      color: white;
      border: none;
      border-radius: 50%;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: background 0.2s;
    }

    .remove-btn:hover {
      background: rgba(220, 38, 38, 0.9);
    }

    .loading-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
      color: var(--text-color, #f8fafc);
    }

    .spinner {
      border: 3px solid rgba(255,255,255,0.1);
      border-top-color: var(--primary-color, #6366f1);
      border-radius: 50%;
      width: 24px;
      height: 24px;
      animation: spin 1s linear infinite;
    }

    @keyframes spin { 
      to { transform: rotate(360deg); } 
    }
  `],
  host: {
    '(dragover)': 'onDragOver($event)',
    '(dragleave)': 'onDragLeave($event)',
    '(drop)': 'onDrop($event)'
  }
})
export class ImageUploaderComponent {
  private uploadService = inject(ImageUploadService);

  imageUrl = model<string | null>(null);
  displayUrl = computed(() => {
    const url = this.imageUrl();
    if (url?.startsWith('holocron://assets/')) {
      return url.replace('holocron://assets/', '/api/images/');
    }
    return url;
  });
  isDragOver = signal(false);
  isUploading = signal(false);

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  triggerFileInput() {
    if (!this.imageUrl() && !this.isUploading()) {
      this.fileInput.nativeElement.click();
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(true);
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(false);
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(false);

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFile(files[0]);
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFile(input.files[0]);
      // Reset input value so the same file can be uploaded again if removed
      input.value = '';
    }
  }

  private async handleFile(file: File) {
    if (!file.type.startsWith('image/')) {
      alert('Please upload an image file.');
      return;
    }

    // 10MB limit enforcement at UI layer
    if (file.size > 10 * 1024 * 1024) {
      alert('File size exceeds 10MB limit.');
      return;
    }

    try {
      this.isUploading.set(true);
      const url = await this.uploadService.uploadImage(file);
      this.imageUrl.set(url);
    } catch (error) {
      console.error('File upload failed', error);
      alert('File upload failed. Please try again.');
    } finally {
      this.isUploading.set(false);
    }
  }

  removeImage(event: Event) {
    event.stopPropagation(); // prevent triggering file input
    this.imageUrl.set(null);
  }
}
