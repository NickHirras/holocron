import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="h-full w-full bg-holocron-base text-holocron-text-primary p-8">
      <div class="max-w-4xl mx-auto mt-8">
        <h1 class="text-3xl font-bold mb-2">Ceremony Dashboard</h1>
        <p class="text-holocron-text-secondary mb-8">
          Welcome, {{ auth.userProfile()?.email }}. Manage your engineering ceremonies here.
        </p>

        <!-- Placeholder Action Card -->
        <div class="bg-holocron-surface hover:bg-holocron-surface-hover transition-colors rounded-xl border border-slate-700/50 p-6 flex items-center justify-between cursor-pointer group">
          <div>
            <h3 class="text-xl font-semibold text-white group-hover:text-holocron-neon-blue transition-colors">
              Create New Ceremony
            </h3>
            <p class="text-sm text-holocron-text-secondary mt-1">
              Start a new "Google Way" Retrospective or Standup from a template.
            </p>
          </div>
          <div class="bg-holocron-base h-12 w-12 rounded-full flex items-center justify-center border border-slate-700/50 group-hover:border-holocron-neon-blue transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-6 h-6 text-holocron-text-secondary group-hover:text-holocron-neon-blue transition-colors">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>
          </div>
        </div>
        
      </div>
    </div>
  `
})
export class DashboardComponent {
  auth = inject(AuthService);
}
