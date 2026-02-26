import { Injectable, inject } from '@angular/core';
import { ANALYTICS_CLIENT } from '../app.config';
import { TeamMetric } from '../../proto-gen/holocron/v1/ceremony_pb';

@Injectable({
    providedIn: 'root'
})
export class AnalyticsService {
    private analyticsClient = inject(ANALYTICS_CLIENT);

    async getTeamHealth(teamId: string, startTimeSeconds: number, endTimeSeconds: number): Promise<TeamMetric[]> {
        try {
            const startTimestamp = { seconds: BigInt(startTimeSeconds), nanos: 0 };
            const endTimestamp = { seconds: BigInt(endTimeSeconds), nanos: 0 };

            const response = await this.analyticsClient.getTeamHealth({
                teamId,
                startTime: startTimestamp,
                endTime: endTimestamp
            });
            return response.metrics;
        } catch (e) {
            console.error('Failed to get team health metrics', e);
            throw e;
        }
    }
}
