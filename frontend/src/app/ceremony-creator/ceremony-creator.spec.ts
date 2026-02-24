import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CeremonyCreator } from './ceremony-creator';

describe('CeremonyCreator', () => {
  let component: CeremonyCreator;
  let fixture: ComponentFixture<CeremonyCreator>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CeremonyCreator]
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
