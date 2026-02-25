import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ImageUploadService {
    private http = inject(HttpClient);

    async uploadImage(file: File): Promise<string> {
        const response = await firstValueFrom(
            this.http.post<{ url: string }>('/upload/image', file, {
                headers: {
                    'Content-Type': file.type || 'application/octet-stream' // Binary upload
                }
            })
        );
        return response.url;
    }
}
