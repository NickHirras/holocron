import { TestBed } from '@angular/core/testing';
declare var jasmine: any;
import { TeamService } from './team.service';
import { TEAM_CLIENT } from '../app.config';
import { TeamSchema, TeamMembershipSchema, TeamMembership_Role } from '../../proto-gen/holocron/v1/ceremony_pb';
import { create } from '@bufbuild/protobuf';

describe('TeamService', () => {
    let service: TeamService;
    let mockTeamClient: any;

    let listTeamsCalled = false;
    let createTeamReq: any = null;

    beforeEach(() => {
        listTeamsCalled = false;
        createTeamReq = null;
        mockTeamClient = {
            listMyTeams: () => {
                listTeamsCalled = true;
                return Promise.resolve({
                    teams: [
                        create(TeamSchema, { id: 'team1', displayName: 'Mobile Engineering' }),
                        create(TeamSchema, { id: 'team2', displayName: 'Platform Team' })
                    ],
                    memberships: [
                        create(TeamMembershipSchema, { teamId: 'team1', role: TeamMembership_Role.LEADER }),
                        create(TeamMembershipSchema, { teamId: 'team2', role: TeamMembership_Role.MEMBER })
                    ]
                });
            },
            createTeam: (req: any) => {
                createTeamReq = req;
                return Promise.resolve({
                    team: create(TeamSchema, { id: 'team3', displayName: 'New Team' })
                });
            },
            joinTeam: () => Promise.resolve({
                membership: create(TeamMembershipSchema, { teamId: 'team3', role: TeamMembership_Role.MEMBER })
            })
        };

        TestBed.configureTestingModule({
            providers: [
                TeamService,
                { provide: TEAM_CLIENT, useValue: mockTeamClient }
            ]
        });
        service = TestBed.inject(TeamService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should fetch teams and set active team to the first one', async () => {
        await service.refreshTeams();
        expect(listTeamsCalled).toBe(true);
        expect(service.teams().length).toBe(2);
        expect(service.activeTeamId()).toBe('team1');
        expect(service.activeTeam()?.team.displayName).toBe('Mobile Engineering');
    });

    it('should allow switching the active team', async () => {
        await service.refreshTeams();
        service.setActiveTeam('team2');
        expect(service.activeTeamId()).toBe('team2');
        expect(service.activeTeam()?.team.displayName).toBe('Platform Team');
    });

    it('should create a new team and set it as active', async () => {
        await service.refreshTeams();
        // Reconfigure mock for the post-create fetch
        mockTeamClient.listMyTeams = () => Promise.resolve({
            teams: [
                create(TeamSchema, { id: 'team1', displayName: 'Mobile Engineering' }),
                create(TeamSchema, { id: 'team2', displayName: 'Platform Team' }),
                create(TeamSchema, { id: 'team3', displayName: 'New Team' })
            ],
            memberships: [
                create(TeamMembershipSchema, { teamId: 'team1', role: TeamMembership_Role.LEADER }),
                create(TeamMembershipSchema, { teamId: 'team2', role: TeamMembership_Role.MEMBER }),
                create(TeamMembershipSchema, { teamId: 'team3', role: TeamMembership_Role.LEADER })
            ]
        });

        await service.createTeam('New Team');
        expect(createTeamReq).toEqual({ displayName: 'New Team' });
        expect(service.teams().length).toBe(3);
        expect(service.activeTeamId()).toBe('team3');
    });
});
