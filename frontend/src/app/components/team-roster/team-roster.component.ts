import { Component, OnInit, inject, input, signal, effect, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TeamService } from '../../services/team.service';
import { TeamMember, TeamMembership_Role } from '../../../proto-gen/holocron/v1/ceremony_pb';

@Component({
    selector: 'app-team-roster',
    standalone: true,
    imports: [CommonModule],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
    <div class="bg-holocron-surface border border-slate-700/50 rounded-2xl p-6 shadow-lg mb-8 mt-12">
      <h3 class="text-xl font-bold text-white mb-6 flex items-center gap-2">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 text-indigo-400">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
        </svg>
        Team Roster
      </h3>

      @if (loading()) {
        <div class="flex justify-center p-6">
          <div class="w-6 h-6 border-2 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin"></div>
        </div>
      } @else {
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="border-b border-slate-700/50 text-slate-400 text-sm">
                <th class="pb-3 font-medium px-4">Member</th>
                <th class="pb-3 font-medium px-4">Role</th>
                <th class="pb-3 font-medium px-4">Status</th>
              </tr>
            </thead>
            <tbody>
              @for (member of members(); track member.user?.id) {
                <tr class="border-b border-slate-700/20 last:border-0 hover:bg-slate-800/30 transition-colors">
                  <td class="py-4 px-4">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-bold text-xs shadow-md">
                        {{ getInitials(member.user?.email || '?') }}
                      </div>
                      <span class="text-white font-medium">{{ member.user?.email }}</span>
                    </div>
                  </td>
                  <td class="py-4 px-4">
                    @if (member.role === Role.LEADER) {
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-500/10 text-amber-500 border border-amber-500/20 shadow-[0_0_10px_rgba(245,158,11,0.1)]">
                        Leader
                      </span>
                    } @else {
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-500/10 text-slate-300 border border-slate-500/20">
                        Member
                      </span>
                    }
                  </td>
                  <td class="py-4 px-4 text-emerald-400 text-sm font-medium flex items-center gap-1.5 mt-2">
                    <span class="w-2 h-2 rounded-full bg-emerald-500"></span> Active
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="3" class="py-8 text-center text-slate-400">No members found.</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `
})
export class TeamRosterComponent implements OnInit {
    teamService = inject(TeamService);

    teamId = input.required<string>();
    members = signal<TeamMember[]>([]);
    loading = signal(true);

    Role = TeamMembership_Role;

    constructor() {
        effect(() => {
            const id = this.teamId();
            if (id) {
                this.loadRoster(id);
            }
        });
    }

    ngOnInit() { }

    async loadRoster(id: string) {
        this.loading.set(true);
        try {
            const roster = await this.teamService.getTeamRoster(id);
            this.members.set(roster.members);
        } catch (e) {
            console.error('Failed to load team roster', e);
        } finally {
            this.loading.set(false);
        }
    }

    getInitials(email: string): string {
        if (!email) return '?';
        return email.charAt(0).toUpperCase();
    }
}
