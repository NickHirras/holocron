import { Injectable, inject } from '@angular/core';
import { CEREMONY_CLIENT } from '../app.config';
import { CeremonyTemplate, CreateCeremonyTemplateRequestSchema, CreateCeremonyTemplateResponse, GetCeremonyTemplateRequestSchema, GetCeremonyTemplateResponse, SubmitCeremonyResponseRequestSchema, SubmitCeremonyResponseResponse, CeremonyResponse, ListCeremonyTemplatesRequestSchema, ListCeremonyTemplatesResponse } from '../../proto-gen/holocron/v1/ceremony_pb';
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

  async listTemplates(): Promise<ListCeremonyTemplatesResponse> {
    const request = create(ListCeremonyTemplatesRequestSchema, {});
    return await this.client.listCeremonyTemplates(request);
  }

  async getTemplate(templateId: string): Promise<GetCeremonyTemplateResponse> {
    const request = create(GetCeremonyTemplateRequestSchema, { templateId });
    return await this.client.getCeremonyTemplate(request);
  }

  async submitResponse(response: CeremonyResponse): Promise<SubmitCeremonyResponseResponse> {
    const request = create(SubmitCeremonyResponseRequestSchema, { response });
    return await this.client.submitCeremonyResponse(request);
  }
}
