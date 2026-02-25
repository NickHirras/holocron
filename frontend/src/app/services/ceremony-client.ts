import { Injectable, inject } from '@angular/core';
import { CEREMONY_CLIENT } from '../app.config';
import { CeremonyTemplate, CreateCeremonyTemplateRequestSchema, CreateCeremonyTemplateResponse, GetCeremonyTemplateRequestSchema, GetCeremonyTemplateResponse, SubmitCeremonyResponseRequestSchema, SubmitCeremonyResponseResponse, CeremonyResponse, ListCeremonyTemplatesRequestSchema, ListCeremonyTemplatesResponse, ListCeremonyResponsesRequestSchema, ListCeremonyResponsesResponse } from '../../proto-gen/holocron/v1/ceremony_pb';
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

  async listTemplates(teamId: string): Promise<ListCeremonyTemplatesResponse> {
    const request = create(ListCeremonyTemplatesRequestSchema, { teamId });
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

  async listResponses(ceremonyTemplateId: string, filterStartDate?: Date, filterEndDate?: Date): Promise<ListCeremonyResponsesResponse> {
    const data: any = { ceremonyTemplateId };

    if (filterStartDate) {
      data.filterStartDate = { seconds: BigInt(Math.floor(filterStartDate.getTime() / 1000)), nanos: 0 };
    }
    if (filterEndDate) {
      data.filterEndDate = { seconds: BigInt(Math.floor(filterEndDate.getTime() / 1000)), nanos: 0 };
    }

    const request = create(ListCeremonyResponsesRequestSchema, data);
    return await this.client.listCeremonyResponses(request);
  }
}
