import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CeremonyClientService } from '../services/ceremony-client';
import { CeremonyTemplate } from '../../proto-gen/holocron/v1/ceremony_pb';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="h-full w-full bg-holocron-base text-holocron-text-primary p-6 md:p-10">
      <div class="max-w-6xl mx-auto">
        <!-- Dashboard Header -->
        <header class="mb-12 flex flex-col md:flex-row md:items-end justify-between gap-4 border-b border-slate-700/50 pb-6">
          <div>
            <h1 class="text-4xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-white to-slate-400 mb-2 tracking-tight">
              Ceremony Dashboard
            </h1>
            <p class="text-holocron-text-secondary text-lg">
              Welcome back, <span class="text-white font-medium">{{ auth.userProfile()?.email }}</span>.
            </p>
          </div>
          
          <button class="flex items-center gap-2 bg-holocron-surface hover:bg-holocron-surface-hover border border-slate-600 rounded-lg px-4 py-2 text-sm font-medium transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4 text-holocron-neon-blue">
              <path stroke-linecap="round" stroke-linejoin="round" d="M10.34 15.84c-.688-.06-1.386-.09-2.09-.09H7.5a4.5 4.5 0 1 1 0-9h.75c.704 0 1.402-.03 2.09-.09m0 9.18c.253.962.584 1.892.985 2.783.247.55.06 1.21-.463 1.511l-.657.38c-.551.318-1.26.117-1.527-.461a20.845 20.845 0 0 1-1.44-4.282m3.102.069a18.03 18.03 0 0 1-.59-4.59c0-1.586.205-3.124.59-4.59m0 9.18a23.848 23.848 0 0 1 8.835 2.535M10.34 6.66a23.847 23.847 0 0 0 8.835-2.535m0 0A23.74 23.74 0 0 0 18.795 3m.38 1.125a23.91 23.91 0 0 1 1.014 5.395m-1.014 8.855c-.118.38-.245.754-.38 1.125m.38-1.125a23.91 23.91 0 0 0 1.014-5.395m0-3.46c.495.413.811 1.035.811 1.73 0 .695-.316 1.317-.811 1.73m0-3.46a24.347 24.347 0 0 1 0 3.46" />
            </svg>
            Recent Activity
          </button>
        </header>

        <h2 class="text-xl font-bold text-white mb-6 flex items-center gap-2">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 text-indigo-400">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
          </svg>
          Quick Actions
        </h2>

        <!-- Action Grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
          
          <!-- Create Standup Card -->
          <div (click)="createTemplate('standup')" class="group relative bg-[#131d30] border border-slate-700/50 hover:border-indigo-500/50 rounded-2xl p-6 cursor-pointer transition-all duration-300 hover:shadow-[0_0_30px_rgba(99,102,241,0.15)] overflow-hidden">
            <div class="absolute inset-0 bg-gradient-to-br from-indigo-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
            <div class="relative z-10 flex flex-col h-full">
              <div class="bg-indigo-500/10 w-12 h-12 rounded-xl flex items-center justify-center border border-indigo-500/20 mb-4 group-hover:bg-indigo-500/20 transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6 text-indigo-400">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6A2.25 2.25 0 0 1 6 3.75h2.25A2.25 2.25 0 0 1 10.5 6v2.25a2.25 2.25 0 0 1-2.25 2.25H6a2.25 2.25 0 0 1-2.25-2.25V6ZM3.75 15.75A2.25 2.25 0 0 1 6 13.5h2.25a2.25 2.25 0 0 1 2.25 2.25V18a2.25 2.25 0 0 1-2.25 2.25H6A2.25 2.25 0 0 1 3.75 18v-2.25ZM13.5 6a2.25 2.25 0 0 1 2.25-2.25H18A2.25 2.25 0 0 1 20.25 6v2.25A2.25 2.25 0 0 1 18 10.5h-2.25a2.25 2.25 0 0 1-2.25-2.25V6ZM13.5 15.75a2.25 2.25 0 0 1 2.25-2.25H18a2.25 2.25 0 0 1 2.25 2.25V18A2.25 2.25 0 0 1 18 20.25h-2.25A2.25 2.25 0 0 1 13.5 18v-2.25Z" />
                </svg>
              </div>
              <h3 class="text-xl font-bold text-white mb-2 group-hover:text-indigo-400 transition-colors">Daily Standup</h3>
              <p class="text-holocron-text-secondary text-sm flex-grow">A quick, focused template for daily alignment: "What did I do? What am I doing? Any blockers?"</p>
            </div>
          </div>

          <!-- Create Retro Card -->
          <div (click)="createTemplate('retro')" class="group relative bg-[#131d30] border border-slate-700/50 hover:border-holocron-neon-blue/50 rounded-2xl p-6 cursor-pointer transition-all duration-300 hover:shadow-[0_0_30px_rgba(56,189,248,0.15)] overflow-hidden">
            <div class="absolute inset-0 bg-gradient-to-br from-holocron-neon-blue/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
            <div class="relative z-10 flex flex-col h-full">
              <div class="bg-holocron-neon-blue/10 w-12 h-12 rounded-xl flex items-center justify-center border border-holocron-neon-blue/20 mb-4 group-hover:bg-holocron-neon-blue/20 transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6 text-holocron-neon-blue">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0 3.181 3.183a8.25 8.25 0 0 0 13.803-3.7M4.031 9.865a8.25 8.25 0 0 1 13.803-3.7l3.181 3.182m0-4.991v4.99" />
                </svg>
              </div>
              <h3 class="text-xl font-bold text-white mb-2 group-hover:text-holocron-neon-blue transition-colors">Sprint Retrospective</h3>
              <p class="text-holocron-text-secondary text-sm flex-grow">Reflect on the past sprint. Create custom categories like "Mad, Sad, Glad" or "Start, Stop, Continue".</p>
            </div>
          </div>
          
          <!-- Blank/Custom Card -->
          <div (click)="createTemplate('custom')" class="group relative bg-[#131d30] border border-dashed border-slate-600 hover:border-slate-400 rounded-2xl p-6 cursor-pointer transition-all duration-300 flex flex-col items-center justify-center text-center">
            <div class="bg-slate-800 w-12 h-12 rounded-full flex items-center justify-center mb-4 group-hover:bg-slate-700 transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-6 h-6 text-slate-300">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
              </svg>
            </div>
            <h3 class="text-lg font-bold text-slate-300 group-hover:text-white transition-colors">Custom Ceremony</h3>
            <p class="text-slate-500 text-sm mt-1">Start from scratch</p>
          </div>

        </div>
        
        <!-- Live Ceremonies List -->
        <h2 class="text-xl font-bold text-white mb-6 flex items-center gap-2 mt-12">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 text-indigo-400">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 6.75h12M8.25 12h12m-12 5.25h12M3.75 6.75h.007v.008H3.75V6.75Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0ZM3.75 12h.007v.008H3.75V12Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm-.375 5.25h.007v.008H3.75v-.008Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Z" />
          </svg>
          Your Ceremonies
        </h2>

        <div *ngIf="loading()" class="text-slate-400">Loading templates...</div>
        <div *ngIf="!loading() && templates().length === 0" class="text-slate-500 italic border border-slate-700/50 rounded p-6 bg-[#131d30]">No ceremonies have been created yet. Get started above.</div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6" *ngIf="!loading() && templates().length > 0">
          <div *ngFor="let tmpl of templates()" (click)="respondToTemplate(tmpl.id)" class="group relative bg-[#131d30] border border-slate-700/50 hover:border-slate-500 rounded-2xl p-6 cursor-pointer transition-all duration-300 hover:-translate-y-1">
              <h3 class="text-lg font-bold text-white mb-2">{{ tmpl.title || 'Untitled Ceremony' }}</h3>
              <p class="text-holocron-text-secondary text-sm line-clamp-2 mb-4">{{ tmpl.description || 'No description provided.' }}</p>
              <div class="flex items-center justify-between text-xs text-slate-500">
                  <span>{{ tmpl.items.length }} Questions</span>
                  <span class="text-indigo-400 group-hover:underline">Respond &rarr;</span>
              </div>
          </div>
        </div>

      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  router = inject(Router);
  ceremonyClient = inject(CeremonyClientService);

  templates = signal<CeremonyTemplate[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.loadTemplates();
  }

  async loadTemplates() {
    try {
      const response = await this.ceremonyClient.listTemplates();
      this.templates.set(response.templates);
    } catch (e) {
      console.error("Failed to load templates", e);
    } finally {
      this.loading.set(false);
    }
  }

  createTemplate(type: string) {
    this.router.navigate(['/create'], { queryParams: { type } });
  }

  respondToTemplate(id: string) {
    this.router.navigate(['/ceremony', id]);
  }
}
