import { Injectable, computed, inject, signal } from '@angular/core';
import { TEAM_CLIENT } from '../app.config';
import { Team, TeamMembership } from '../../proto-gen/holocron/v1/ceremony_pb';

export interface TeamWithMembership {
    team: Team;
    membership: TeamMembership;
}

@Injectable({
    providedIn: 'root'
})
export class TeamService {
    private teamClient = inject(TEAM_CLIENT);

    private readonly _teams = signal<TeamWithMembership[]>([]);
    private readonly _activeTeamId = signal<string | null>(null);

    // Publicly exposed readonly state derived from the private signal
    public readonly teams = this._teams.asReadonly();
    public readonly activeTeamId = this._activeTeamId.asReadonly();
    public readonly activeTeam = computed(() => {
        const tId = this._activeTeamId();
        if (!tId) return undefined;
        return this._teams().find(t => t.team?.id === tId);
    });
    public readonly activeTeamRole = computed(() => {
        const active = this.activeTeam();
        return active?.membership?.role;
    });

    constructor() {
        // Will be called when user logs in, or we explicitly initialize it 
        // We'll call it explicitly from the dashboard for now
    }

    async refreshTeams() {
        try {
            const res = await this.teamClient.listMyTeams({});
            const teamsWithMem = res.teams.map((t, idx) => ({
                team: t,
                membership: res.memberships[idx]
            }));
            this._teams.set(teamsWithMem);
            if (teamsWithMem.length > 0 && !this._activeTeamId()) {
                this._activeTeamId.set(teamsWithMem[0].team!.id);
            }
        } catch (e) {
            console.error('❌ Failed to load teams:', e);
        }
    }

    async createTeam(name: string) {
        if (!name?.trim()) return;
        try {
            const res = await this.teamClient.createTeam({ displayName: name });
            await this.refreshTeams();
            if (res.team?.id) {
                this.setActiveTeam(res.team.id);
            }
        } catch (e) {
            console.error('❌ Failed to create team:', e);
            throw e;
        }
    }

    async joinTeam(teamId: string) {
        if (!teamId?.trim()) return;
        try {
            await this.teamClient.joinTeam({ teamId });
            await this.refreshTeams();
            this.setActiveTeam(teamId);
        } catch (e) {
            console.error('❌ Failed to join team:', e);
            throw e;
        }
    }

    setActiveTeam(teamId: string) {
        this._activeTeamId.set(teamId);
    }
}
