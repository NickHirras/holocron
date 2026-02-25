import { TestBed } from '@angular/core/testing';

import { CeremonyClientService } from './ceremony-client';

describe('CeremonyClient', () => {
  let service: CeremonyClientService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CeremonyClientService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
