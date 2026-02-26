import { TestBed } from '@angular/core/testing';

import { CeremonyClientService } from './ceremony-client';
import { CEREMONY_CLIENT } from '../app.config';

describe('CeremonyClient', () => {
  let service: CeremonyClientService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: CEREMONY_CLIENT, useValue: {} }
      ]
    });
    service = TestBed.inject(CeremonyClientService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
