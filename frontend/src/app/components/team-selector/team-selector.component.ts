import { Component, inject, computed, signal, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TeamService } from '../../services/team.service';

@Component({
  selector: 'app-team-selector',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="relative inline-block text-left">
      <div>
        <button type="button" (click)="isOpen.set(!isOpen())" class="flex items-center gap-2 bg-[#1a2332] hover:bg-slate-800 border border-slate-600 rounded-lg px-3 py-1.5 text-sm font-medium text-white transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500/50 relative z-50">
          <span class="truncate max-w-[150px]">{{ activeTeamName() }}</span>
          <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </button>
      </div>

      <!-- Invisible backdrop for closing dropdown -->
      <div *ngIf="isOpen()" (click)="isOpen.set(false)" class="fixed inset-0 z-40 bg-transparent"></div>

      <div *ngIf="isOpen()" class="absolute left-0 mt-2 w-56 rounded-xl shadow-lg bg-[#1a2332] border border-slate-700 ring-1 ring-black ring-opacity-5 z-50 overflow-hidden transform opacity-100 scale-100 transition-all origin-top-left">
        <div class="py-1">
          <button *ngFor="let tm of teamService.teams()" (click)="selectTeam(tm.team!.id)" class="w-full text-left flex items-center px-4 py-2.5 text-sm text-slate-300 hover:bg-indigo-500/10 hover:text-indigo-400 transition-colors">
             <span class="truncate" [class.font-bold]="tm.team!.id === teamId()" [class.text-white]="tm.team!.id === teamId()">{{ tm.team!.displayName }}</span>
             <svg *ngIf="tm.team!.id === teamId()" xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-auto text-indigo-400" viewBox="0 0 20 20" fill="currentColor">
               <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
             </svg>
          </button>
          <div *ngIf="teamService.teams().length === 0" class="px-4 py-2 text-sm text-slate-500 italic">
            No teams available
          </div>
        </div>
      </div>
    </div>
  `
})
export class TeamSelectorComponent {
  teamService = inject(TeamService);
  router = inject(Router);

  teamId = input.required<string>();
  isOpen = signal(false);

  activeTeamName = computed(() => {
    const tm = this.teamService.teams().find(t => t.team?.id === this.teamId());
    return tm?.team?.displayName || 'Select Team';
  });

  selectTeam(newTeamId: string) {
    this.isOpen.set(false);
    if (newTeamId !== this.teamId()) {
      // Pushing the user to the dashboard of the newly selected team
      this.router.navigate(['/team', newTeamId, 'dashboard']);
    }
  }
}
