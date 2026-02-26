import { Component, Input, OnInit, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsService } from '../../services/analytics.service';
import { TeamMetric } from '../../../proto-gen/holocron/v1/ceremony_pb';

@Component({
    selector: 'app-team-analytics',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="bg-[#131d30] border border-slate-700/50 rounded-2xl p-6 mb-12 shadow-lg">
      <h3 class="text-xl font-bold text-white mb-6 flex items-center gap-2">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-6 h-6 text-indigo-400">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 0 1 3 19.875v-6.75ZM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V8.625ZM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V4.125Z" />
        </svg>
        Team Health Trends
      </h3>

      <div *ngIf="loading()" class="flex items-center justify-center p-8">
        <div class="w-6 h-6 border-2 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin"></div>
        <span class="ml-3 text-slate-400">Analyzing trends...</span>
      </div>

      <div *ngIf="!loading() && metrics().length === 0" class="text-slate-400 text-sm p-4 text-center border border-dashed border-slate-700/50 rounded-lg">
        Not enough data to calculate health trends.
      </div>

      <div *ngIf="!loading() && metrics().length > 0" class="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        <!-- Participation Rate -->
        <div class="bg-[#1a2332] rounded-xl p-5 border border-slate-700/50 relative overflow-hidden group hover:border-indigo-500/50 transition-colors">
          <div class="absolute top-0 right-0 w-24 h-24 bg-emerald-500/5 rounded-full blur-2xl group-hover:bg-emerald-500/10 transition-colors"></div>
          <div class="text-sm text-slate-400 mb-1 font-medium">Participation Rate</div>
          <div class="text-3xl font-bold text-white flex items-baseline gap-2">
            {{ getMetricValue('Participation Rate') | number:'1.0-1' }}<span class="text-lg text-slate-500">%</span>
          </div>
          <div class="w-full bg-slate-800 rounded-full h-1.5 mt-4 overflow-hidden">
            <div class="bg-emerald-500 h-1.5 rounded-full" [style.width.%]="getMetricValue('Participation Rate')"></div>
          </div>
        </div>

        <!-- Sentiment Trend -->
        <div class="bg-[#1a2332] rounded-xl p-5 border border-slate-700/50 relative overflow-hidden group hover:border-amber-500/50 transition-colors">
          <div class="absolute top-0 right-0 w-24 h-24 bg-amber-500/5 rounded-full blur-2xl group-hover:bg-amber-500/10 transition-colors"></div>
          <div class="text-sm text-slate-400 mb-1 font-medium">Sentiment Trend</div>
          <div class="text-3xl font-bold text-white">
            {{ getMetricValue('Sentiment Trend') | number:'1.1-1' }}<span class="text-lg text-slate-500">/5.0</span>
          </div>
          <div class="mt-4 flex items-end gap-1 h-8">
            <!-- Mock sparkline for visual flair -->
            <div class="w-1/5 bg-amber-500/30 rounded-t animation-delay-100" style="height: 40%"></div>
            <div class="w-1/5 bg-amber-500/50 rounded-t animation-delay-200" style="height: 60%"></div>
            <div class="w-1/5 bg-amber-500/40 rounded-t animation-delay-300" style="height: 50%"></div>
            <div class="w-1/5 bg-amber-500/70 rounded-t animation-delay-400" style="height: 80%"></div>
            <div class="w-1/5 bg-amber-500 rounded-t animation-delay-500" [style.height.%]="(getMetricValue('Sentiment Trend') / 5) * 100"></div>
          </div>
        </div>

        <!-- Blocker Count -->
        <div class="bg-[#1a2332] rounded-xl p-5 border border-slate-700/50 relative overflow-hidden group hover:border-rose-500/50 transition-colors">
          <div class="absolute top-0 right-0 w-24 h-24 bg-rose-500/5 rounded-full blur-2xl group-hover:bg-rose-500/10 transition-colors"></div>
          <div class="text-sm text-slate-400 mb-1 font-medium">Recurring Blockers</div>
          <div class="text-3xl font-bold text-white flex items-center gap-3">
            {{ getMetricValue('Blocker Count') }}
            <span *ngIf="getMetricValue('Blocker Count') > 0" class="flex h-3 w-3 relative">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-rose-400 opacity-75"></span>
              <span class="relative inline-flex rounded-full h-3 w-3 bg-rose-500"></span>
            </span>
          </div>
          <div class="text-xs text-rose-400 mt-4 bg-rose-500/10 px-2 py-1 rounded inline-block" *ngIf="getMetricValue('Blocker Count') > 0">
            Requires Attention
          </div>
          <div class="text-xs text-slate-500 mt-4 bg-slate-800 px-2 py-1 rounded inline-block" *ngIf="getMetricValue('Blocker Count') === 0">
            No blockers detected
          </div>
        </div>

      </div>
    </div>
  `
})
export class TeamAnalyticsComponent {
    @Input()
    set teamId(id: string) {
        if (this._teamId !== id) {
            this._teamId = id;
            this.loadMetrics();
        }
    }

    private _teamId: string = '';
    private analyticsService = inject(AnalyticsService);

    loading = signal(false);
    metrics = signal<TeamMetric[]>([]);

    async loadMetrics() {
        if (!this._teamId) return;

        this.loading.set(true);
        try {
            const now = Math.floor(Date.now() / 1000);
            const fourWeeksAgo = now - (4 * 7 * 24 * 60 * 60); // Roughly 4 weeks
            const data = await this.analyticsService.getTeamHealth(this._teamId, fourWeeksAgo, now);
            this.metrics.set(data);
        } catch (error) {
            console.error('Failed to load team analytics:', error);
        } finally {
            this.loading.set(false);
        }
    }

    getMetricValue(name: string): number {
        const metric = this.metrics().find(m => m.metricName === name);
        return metric ? metric.value : 0;
    }
}
