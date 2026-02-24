import { TestBed } from '@angular/core/testing';

import { CeremonyClient } from './ceremony-client';

describe('CeremonyClient', () => {
  let service: CeremonyClient;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CeremonyClient);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
