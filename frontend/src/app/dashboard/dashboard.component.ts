import { Component, inject, OnInit, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CeremonyClientService } from '../services/ceremony-client';
import { TeamService } from '../services/team.service';
import { ActiveCeremony, ResponseStatus, TeamMembership_Role } from '../../proto-gen/holocron/v1/ceremony_pb';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="h-full w-full bg-holocron-base text-holocron-text-primary p-6 md:p-10">
      <div class="max-w-6xl mx-auto">
        <!-- Dashboard Header -->
        <header class="mb-12 flex flex-col md:flex-row md:items-end justify-between gap-4 border-b border-slate-700/50 pb-6">
          <div>
            <h1 class="text-4xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-white to-slate-400 mb-2 tracking-tight">
              Ceremony Dashboard
            </h1>
            <p class="text-holocron-text-secondary text-lg mt-2 flex items-center gap-4">
              <span>Welcome back, <span class="text-white font-medium">{{ auth.userProfile()?.email }}</span>.</span>
              
              <span class="w-px h-5 bg-slate-700"></span>

              <ng-container *ngIf="teamService.teams().length > 0">
                <select [ngModel]="teamService.activeTeamId()" (ngModelChange)="switchTeam($event)" class="bg-[#1a2332] border border-slate-600 rounded px-2 py-1 text-sm text-white focus:outline-none focus:border-indigo-500">
                  <option *ngFor="let tm of teamService.teams()" [value]="tm.team?.id">{{ tm.team?.displayName }}</option>
                </select>
              </ng-container>
              
              <button *ngIf="teamService.activeTeamRole() === 2" (click)="isCreatingTeam.set(true)" class="text-xs text-indigo-400 hover:text-indigo-300 transition-colors bg-indigo-500/10 px-2 py-1 rounded border border-indigo-500/20">
                + New Team
              </button>
            </p>
          </div>
          
          <button (click)="isActivityDrawerOpen.set(true)" class="flex items-center gap-2 bg-holocron-surface hover:bg-holocron-surface-hover border border-slate-600 rounded-lg px-4 py-2 text-sm font-medium transition-colors mt-4 md:mt-0">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4 text-holocron-neon-blue">
              <path stroke-linecap="round" stroke-linejoin="round" d="M10.34 15.84c-.688-.06-1.386-.09-2.09-.09H7.5a4.5 4.5 0 1 1 0-9h.75c.704 0 1.402-.03 2.09-.09m0 9.18c.253.962.584 1.892.985 2.783.247.55.06 1.21-.463 1.511l-.657.38c-.551.318-1.26.117-1.527-.461a20.845 20.845 0 0 1-1.44-4.282m3.102.069a18.03 18.03 0 0 1-.59-4.59c0-1.586.205-3.124.59-4.59m0 9.18a23.848 23.848 0 0 1 8.835 2.535M10.34 6.66a23.847 23.847 0 0 0 8.835-2.535m0 0A23.74 23.74 0 0 0 18.795 3m.38 1.125a23.91 23.91 0 0 1 1.014 5.395m-1.014 8.855c-.118.38-.245.754-.38 1.125m.38-1.125a23.91 23.91 0 0 0 1.014-5.395m0-3.46c.495.413.811 1.035.811 1.73 0 .695-.316 1.317-.811 1.73m0-3.46a24.347 24.347 0 0 1 0 3.46" />
            </svg>
            Recent Activity
          </button>
        </header>

        <!-- Quick Actions (Only for Leaders) -->
        <ng-container *ngIf="isLeader()">
          <h2 class="text-xl font-bold text-white mb-6 flex items-center gap-2">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 text-indigo-400">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>
            Manage Ceremonies
          </h2>

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
                <p class="text-holocron-text-secondary text-sm flex-grow">A quick, focused template for daily alignment.</p>
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
                <p class="text-holocron-text-secondary text-sm flex-grow">Reflect on the past sprint with custom categories.</p>
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
        </ng-container>

        <!-- Your Daily Rituals List (PENDING) -->
        <h2 class="text-xl font-bold text-white mb-6 flex items-center gap-2 mt-12">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 text-indigo-400">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 6.75h12M8.25 12h12m-12 5.25h12M3.75 6.75h.007v.008H3.75V6.75Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0ZM3.75 12h.007v.008H3.75V12Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm-.375 5.25h.007v.008H3.75v-.008Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Z" />
          </svg>
          Tasks for You
        </h2>

        <div *ngIf="loading()" class="flex flex-col items-center justify-center p-12 space-y-4">
          <div class="w-8 h-8 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin"></div>
          <p class="text-slate-400 font-medium animate-pulse">Loading inbox...</p>
        </div>
        
        <div *ngIf="!loading() && pendingCeremonies().length === 0" class="flex flex-col items-center justify-center p-12 text-center bg-gradient-to-b from-[#131d30] to-transparent border border-slate-700/50 rounded-2xl">
          <div class="bg-indigo-500/10 w-20 h-20 rounded-full flex items-center justify-center mb-6">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-10 h-10 text-indigo-400">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-white mb-2">You're all caught up!</h3>
          <p class="text-slate-400 max-w-md mb-8">No pending rituals require your attention. Have a great day!</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6" *ngIf="!loading() && pendingCeremonies().length > 0">
          <div *ngFor="let ceremony of pendingCeremonies()" (click)="respondToTemplate(ceremony.template!.id)" class="group relative bg-[#131d30] border-2 border-indigo-500/50 rounded-2xl p-6 cursor-pointer transition-all duration-300 hover:-translate-y-1 shadow-[0_0_15px_rgba(99,102,241,0.2)]">
              <h3 class="text-xl font-bold text-white mb-2 flex items-start gap-2 overflow-hidden">
                <span class="truncate" title="{{ ceremony.template?.title || 'Untitled Ceremony' }}">{{ ceremony.template?.title || 'Untitled Ceremony' }}</span>
              </h3>
              <p class="text-holocron-text-secondary text-sm line-clamp-2 mb-4 break-all">{{ ceremony.template?.description || 'No description provided.' }}</p>
              <div class="flex items-center justify-between mt-4">
                  <span class="text-xs px-2 py-1 rounded bg-amber-500/20 text-amber-400 border border-amber-500/30 font-medium">Needs Response</span>
                  <span class="text-indigo-400 font-bold group-hover:underline flex items-center gap-1">
                    Start <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4"><path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5 21 12m0 0-7.5 7.5M21 12H3" /></svg>
                  </span>
              </div>
          </div>
        </div>

        <!-- Completed / Team Pulse section -->
        <ng-container *ngIf="!loading() && completedCeremonies().length > 0">
            <h2 class="text-xl font-bold text-white mb-6 flex items-center gap-2 mt-12">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 text-emerald-400">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
              </svg>
              Completed / Team Pulse
            </h2>

            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <div *ngFor="let ceremony of completedCeremonies()" (click)="viewResults(ceremony.template!.id)" class="group relative bg-[#131d30] border border-slate-700/50 hover:border-slate-500 opacity-80 hover:opacity-100 rounded-2xl p-6 cursor-pointer transition-all duration-300">
                  <h3 class="text-lg font-bold text-white mb-2 flex items-start gap-2 overflow-hidden">
                    <span class="truncate" title="{{ ceremony.template?.title || 'Untitled Ceremony' }}">{{ ceremony.template?.title || 'Untitled Ceremony' }}</span>
                  </h3>
                  <p class="text-holocron-text-secondary text-sm line-clamp-2 mb-4 break-all">{{ ceremony.template?.description || 'No description provided.' }}</p>
                  <div class="flex items-center justify-between mt-4">
                      <span class="text-xs px-2 py-1 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-medium">Completed</span>
                      <span class="text-slate-400 group-hover:text-emerald-400 group-hover:underline flex items-center gap-1">
                        View Results <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4"><path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5 21 12m0 0-7.5 7.5M21 12H3" /></svg>
                      </span>
                  </div>
              </div>
            </div>
        </ng-container>

      </div>
    </div>

    <!-- Create Team Modal (Unchanged) -->
    <div *ngIf="isCreatingTeam()" class="fixed inset-0 bg-[#0f172a]/90 backdrop-blur-sm z-[120] flex items-center justify-center p-4">
      <div class="bg-[#1a2332] border border-slate-700/50 rounded-2xl w-full max-w-md shadow-2xl overflow-hidden">
        <div class="p-6 border-b border-slate-700/50 flex justify-between items-center">
          <h2 class="text-xl font-bold text-white">Create New Team</h2>
          <button (click)="isCreatingTeam.set(false)" class="text-slate-400 hover:text-white transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
          </button>
        </div>
        <div class="p-6">
          <label class="block text-sm font-medium text-slate-400 mb-2">Team Name</label>
          <input #teamNameInput type="text" placeholder="e.g. Mobile Engineering" class="w-full bg-[#131d30] border border-slate-600 rounded-lg px-4 py-3 text-white focus:outline-none focus:border-indigo-500 mb-6" (keyup.enter)="createTeam(teamNameInput.value)">
          <div class="flex justify-end gap-3">
            <button (click)="isCreatingTeam.set(false)" class="px-4 py-2 text-slate-300 hover:text-white font-medium transition-colors">Cancel</button>
            <button (click)="createTeam(teamNameInput.value)" class="bg-indigo-500 hover:bg-indigo-600 text-white px-6 py-2 rounded-lg font-medium shadow-[0_0_15px_rgba(99,102,241,0.4)] transition-all">Create</button>
          </div>
        </div>
      </div>
    </div>

    <!-- No Teams State (Unchanged) -->
    <div *ngIf="!loading() && teamService.teams().length === 0" class="fixed inset-0 bg-[#0f172a]/95 backdrop-blur-md z-[130] flex items-center justify-center p-4">
      <div class="bg-[#1a2332] border border-indigo-500/30 rounded-2xl w-full max-w-lg p-8 shadow-[0_0_50px_rgba(99,102,241,0.15)] text-center">
        <div class="w-16 h-16 bg-indigo-500/20 rounded-full flex items-center justify-center mx-auto mb-6">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-8 h-8 text-indigo-400">
            <path stroke-linecap="round" stroke-linejoin="round" d="M18 18.72a9.094 9.094 0 0 0 3.741-.479 3 3 0 0 0-4.682-2.72m.94 3.198.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0 1 12 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 0 1 6 18.719m12 0a5.971 5.971 0 0 0-.941-3.197m0 0A5.995 5.995 0 0 0 12 12.75a5.995 5.995 0 0 0-5.058 2.772m0 0a3 3 0 0 0-4.681 2.72 8.986 8.986 0 0 0 3.74.477m.94-3.197a5.971 5.971 0 0 0-.94 3.197M15 6.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Zm6 3a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Zm-13.5 0a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Z" />
          </svg>
        </div>
        <h2 class="text-2xl font-bold text-white mb-3">Welcome to Holocron Teams</h2>
        <p class="text-slate-400 mb-8">Before you can participate, you need to join or create a team.</p>
        <div class="space-y-4">
          <div>
            <input #firstTeamInput type="text" placeholder="Enter a team name to create..." class="w-full bg-[#131d30] border border-slate-600 rounded-lg px-4 py-3 text-white focus:outline-none focus:border-indigo-500 mb-3" (keyup.enter)="createTeam(firstTeamInput.value)">
            <button (click)="createTeam(firstTeamInput.value)" class="w-full bg-indigo-500 hover:bg-indigo-600 text-white py-3 rounded-lg font-bold shadow-[0_0_15px_rgba(99,102,241,0.5)] transition-all">Create First Team</button>
          </div>
          <div class="relative flex py-2 items-center">
             <div class="flex-grow border-t border-slate-700/80"></div>
             <span class="flex-shrink-0 mx-4 text-slate-500 text-sm">or</span>
             <div class="flex-grow border-t border-slate-700/80"></div>
          </div>
          <div class="flex gap-2">
            <input #joinTeamInput type="text" placeholder="Paste existing Team ID..." class="flex-1 bg-[#131d30] border border-slate-600 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-emerald-500" (keyup.enter)="joinTeam(joinTeamInput.value)">
             <button (click)="joinTeam(joinTeamInput.value)" class="bg-emerald-500 hover:bg-emerald-600 text-white px-4 py-2 rounded-lg font-bold shadow-[0_0_15px_rgba(16,185,129,0.3)] transition-all">Join</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Activity Drawer Backdrop -->
    <div *ngIf="isActivityDrawerOpen()" (click)="isActivityDrawerOpen.set(false)" class="fixed inset-0 bg-[#0f172a]/80 backdrop-blur-sm z-[100] transition-opacity"></div>
    
    <!-- Recent Activity Drawer Panel -->
    <div class="fixed top-0 right-0 h-full w-full sm:w-[500px] bg-[#1a2332] border-l border-slate-700/50 shadow-2xl z-[110] transform transition-transform duration-300 ease-in-out flex flex-col" [class.translate-x-full]="!isActivityDrawerOpen()" [class.translate-x-0]="isActivityDrawerOpen()">
      
      <!-- Drawer Header -->
      <div class="p-6 border-b border-slate-700/50 flex justify-between items-center bg-[#131d30]">
        <h2 class="text-xl font-bold text-white flex items-center gap-2">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 text-holocron-neon-blue">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
          </svg>
          Activity Feed
        </h2>
        <button (click)="isActivityDrawerOpen.set(false)" class="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-colors">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Drawer Content (Mock Data for Polish) -->
      <div class="flex-1 overflow-y-auto p-6 scrollbar-thin scrollbar-thumb-slate-600 scrollbar-track-transparent">
        <div class="space-y-6 relative before:absolute before:inset-0 before:ml-5 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-gradient-to-b before:from-transparent before:via-slate-700/50 before:to-transparent">
          
          <!-- Mock Activity Item 1 -->
          <div class="relative flex items-center justify-between md:justify-normal md:odd:flex-row-reverse group is-active">
            <div class="flex items-center justify-center w-10 h-10 rounded-full border border-white bg-slate-800 text-slate-500 shadow shrink-0 md:order-1 md:group-odd:-translate-x-1/2 md:group-even:translate-x-1/2">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 text-emerald-400">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 0 1 1.04 0l2.125 5.111a.563.563 0 0 0 .475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 0 0-.182.557l1.285 5.385a.562.562 0 0 1-.84.61l-4.725-2.885a.562.562 0 0 0-.586 0L6.982 20.54a.562.562 0 0 1-.84-.61l1.285-5.386a.562.562 0 0 0-.182-.557l-4.204-3.602a.562.562 0 0 1 .321-.988l5.518-.442a.563.563 0 0 0 .475-.345L11.48 3.5Z" />
              </svg>
            </div>
            <div class="w-[calc(100%-4rem)] md:w-[calc(50%-2.5rem)] bg-[#131d30] border border-slate-700/50 p-4 rounded-xl shadow-[0_0_15px_rgba(0,0,0,0.2)]">
              <div class="flex items-center justify-between space-x-2 mb-1">
                <div class="font-bold text-slate-200">New Template Created</div>
                <time class="font-caveat font-medium text-emerald-400">Just now</time>
              </div>
              <div class="text-slate-400 text-sm">You created a new "Daily Standup" template. Ready to collect responses!</div>
            </div>
          </div>

        </div>
        
        <div class="mt-8 text-center border-t border-slate-800/50 pt-6">
            <p class="text-slate-500 text-sm">End of recent activity</p>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  router = inject(Router);
  ceremonyClient = inject(CeremonyClientService);
  teamService = inject(TeamService);

  activeCeremonies = signal<ActiveCeremony[]>([]);
  loading = signal(true);
  isActivityDrawerOpen = signal(false);
  isCreatingTeam = signal(false);

  pendingCeremonies = computed(() => {
    return this.activeCeremonies().filter(ac => ac.responseStatus === ResponseStatus.PENDING);
  });

  completedCeremonies = computed(() => {
    return this.activeCeremonies().filter(ac => ac.responseStatus === ResponseStatus.COMPLETED);
  });

  isLeader = computed(() => {
    return this.teamService.activeTeamRole() === TeamMembership_Role.LEADER;
  });

  constructor() {
    effect(() => {
      const activeTeamId = this.teamService.activeTeamId();
      if (activeTeamId) {
        this.loadTemplates(activeTeamId);
      } else {
        this.activeCeremonies.set([]);
      }
    });
  }

  async ngOnInit() {
    await this.teamService.refreshTeams();
    this.loading.set(false);
  }

  async loadTemplates(teamId: string) {
    this.loading.set(true);
    try {
      const response = await this.ceremonyClient.listActiveCeremonies(teamId);
      this.activeCeremonies.set(response.activeCeremonies);
    } catch (e) {
      console.error("Failed to load templates", e);
    } finally {
      this.loading.set(false);
    }
  }

  switchTeam(teamId: string) {
    this.teamService.setActiveTeam(teamId);
  }

  async createTeam(name: string) {
    if (!name.trim()) return;
    this.loading.set(true);
    try {
      await this.teamService.createTeam(name);
      this.isCreatingTeam.set(false);
    } catch (e) {
      console.error("Failed to create team", e);
    } finally {
      this.loading.set(false);
    }
  }

  async joinTeam(teamId: string) {
    if (!teamId.trim()) return;
    this.loading.set(true);
    try {
      await this.teamService.joinTeam(teamId);
    } catch (e) {
      console.error("Failed to join team", e);
    } finally {
      this.loading.set(false);
    }
  }

  createTemplate(type: string) {
    const activeTeam = this.teamService.activeTeamId();
    if (!activeTeam) return;
    this.router.navigate(['/create'], { queryParams: { type, teamId: activeTeam } });
  }

  respondToTemplate(id: string) {
    this.router.navigate(['/ceremony', id]);
  }

  viewResults(id: string) {
    this.router.navigate(['/create', id, 'results']);
  }
}
