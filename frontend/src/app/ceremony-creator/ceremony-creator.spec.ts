import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CeremonyCreator } from './ceremony-creator';
import { ActivatedRoute } from '@angular/router';
import { CeremonyClientService } from '../services/ceremony-client';
import { CEREMONY_CLIENT } from '../app.config';

describe('CeremonyCreator', () => {
  let component: CeremonyCreator;
  let fixture: ComponentFixture<CeremonyCreator>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CeremonyCreator],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => 'test-id' } },
            paramMap: {
              pipe: () => ({
                subscribe: () => { }
              })
            },
            queryParams: {
              pipe: () => ({
                subscribe: () => { }
              })
            }
          }
        },
        { provide: CeremonyClientService, useValue: {} },
        { provide: CEREMONY_CLIENT, useValue: {} }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CeremonyCreator);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
