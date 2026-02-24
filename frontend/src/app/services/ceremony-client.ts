import { Injectable, inject } from '@angular/core';
import { CEREMONY_CLIENT } from '../app.config';
import { CeremonyTemplate, CreateCeremonyTemplateRequestSchema, CreateCeremonyTemplateResponse } from '../../proto-gen/holocron/v1/ceremony_pb';
import { create } from '@bufbuild/protobuf';

@Injectable({
  providedIn: 'root',
})
export class CeremonyClientService {
  private client = inject(CEREMONY_CLIENT);

  async createTemplate(template: CeremonyTemplate): Promise<CreateCeremonyTemplateResponse> {
    const request = create(CreateCeremonyTemplateRequestSchema, { template });
    return await this.client.createCeremonyTemplate(request);
  }
}
