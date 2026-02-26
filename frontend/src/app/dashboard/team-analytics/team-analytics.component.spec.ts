import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
declare var jasmine: any;
import { TeamAnalyticsComponent } from './team-analytics.component';
import { AnalyticsService } from '../../services/analytics.service';
import { TeamMetric, TeamMetricSchema } from '../../../proto-gen/holocron/v1/ceremony_pb';
import { create } from '@bufbuild/protobuf';

describe('TeamAnalyticsComponent', () => {
    let component: TeamAnalyticsComponent;
    let fixture: ComponentFixture<TeamAnalyticsComponent>;
    let getTeamHealthCalled = false;
    let mockAnalyticsService = {
        getTeamHealth: () => {
            getTeamHealthCalled = true;
            return Promise.resolve([
                create(TeamMetricSchema, { metricName: 'Participation Rate', value: 80 }),
                create(TeamMetricSchema, { metricName: 'Sentiment Trend', value: 4.5 }),
                create(TeamMetricSchema, { metricName: 'Blocker Count', value: 2 })
            ]);
        }
    };

    beforeEach(async () => {
        getTeamHealthCalled = false;
        await TestBed.configureTestingModule({
            imports: [TeamAnalyticsComponent],
            providers: [
                { provide: AnalyticsService, useValue: mockAnalyticsService }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(TeamAnalyticsComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load metrics when teamId changes', async () => {
        component.teamId = 'team-1';

        // Wait for async loadMetrics to finish
        await fixture.whenStable();

        expect(getTeamHealthCalled).toBe(true);
        expect(component.metrics().length).toBe(3);

        expect(component.getMetricValue('Participation Rate')).toBe(80);
        expect(component.getMetricValue('Sentiment Trend')).toBe(4.5);
        expect(component.getMetricValue('Blocker Count')).toBe(2);
    });
});
