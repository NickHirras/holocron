import { TestBed } from '@angular/core/testing';
declare var jasmine: any;
import { AnalyticsService } from './analytics.service';
import { ANALYTICS_CLIENT } from '../app.config';
import { TeamMetric, TeamMetricSchema } from '../../proto-gen/holocron/v1/ceremony_pb';
import { create } from '@bufbuild/protobuf';

describe('AnalyticsService', () => {
    let service: AnalyticsService;
    let getTeamHealthCalled = false;
    let mockClient = {
        getTeamHealth: () => {
            getTeamHealthCalled = true;
            return Promise.resolve({
                metrics: [
                    create(TeamMetricSchema, { metricName: 'Participation Rate', value: 80.0 })
                ]
            });
        }
    };

    beforeEach(() => {
        getTeamHealthCalled = false;
        TestBed.configureTestingModule({
            providers: [
                AnalyticsService,
                { provide: ANALYTICS_CLIENT, useValue: mockClient }
            ]
        });
        service = TestBed.inject(AnalyticsService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should call getTeamHealth and return metrics', async () => {
        const result = await service.getTeamHealth('team-1', 12345, 67890);
        expect(getTeamHealthCalled).toBe(true);
        expect(result.length).toBe(1);
        expect(result[0].value).toBe(80.0);
    });
});
