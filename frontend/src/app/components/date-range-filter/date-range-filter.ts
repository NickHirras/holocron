import { Component, input, output, computed, signal, effect, untracked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export type DatePreset = 'today' | 'this_week' | 'this_month' | 'past_30_days' | 'ytd' | 'past_year' | 'custom';

export interface DateRange {
  start: string;
  end: string;
}

@Component({
  selector: 'app-date-range-filter',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './date-range-filter.html',
  styles: `
    .date-filter-container {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 8px;
      padding: 0.25rem 0.5rem;
    }
    
    .preset-select {
      background: transparent;
      color: #fff;
      border: none;
      padding: 0.25rem 0.5rem;
      outline: none;
      font-size: 0.95rem;
      cursor: pointer;
    }

    .preset-select option {
      background: #1e293b;
      color: #fff;
    }

    .custom-dates {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      border-left: 1px solid rgba(255, 255, 255, 0.2);
      padding-left: 0.5rem;
    }
    
    .date-input {
      background: transparent;
      color: #fff;
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 4px;
      padding: 0.2rem 0.4rem;
      color-scheme: dark;
      font-size: 0.9rem;
    }

    .paging-controls {
      display: flex;
      align-items: center;
      gap: 0.25rem;
      border-left: 1px solid rgba(255, 255, 255, 0.2);
      padding-left: 0.5rem;
    }

    .page-btn {
      background: transparent;
      border: none;
      color: #94a3b8;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      padding: 0.2rem;
      transition: all 0.2s;
    }

    .page-btn:hover:not(:disabled) {
      background: rgba(255, 255, 255, 0.1);
      color: #fff;
    }

    .page-btn:disabled {
      opacity: 0.3;
      cursor: not-allowed;
    }

    .page-btn .material-symbols-outlined {
      font-size: 1.25rem;
    }
  `
})
export class DateRangeFilter {
  rangeChange = output<DateRange>();

  selectedPreset = signal<DatePreset>('today');

  // These represent the explicitly selected or computed boundaries
  customStart = signal<string>(this.formatDate(new Date()));
  customEnd = signal<string>(this.formatDate(new Date()));

  constructor() {
    effect(() => {
      // Re-emit whenever the actual range changes
      const start = this.customStart();
      const end = this.customEnd();

      untracked(() => {
        this.rangeChange.emit({ start, end });
      });
    });
  }

  onPresetChange(newPreset: DatePreset) {
    this.selectedPreset.set(newPreset);
    this.applyPreset(newPreset);
  }

  onCustomDateChange() {
    this.selectedPreset.set('custom');
  }

  private applyPreset(preset: DatePreset) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    let start = new Date(today);
    let end = new Date(today);

    switch (preset) {
      case 'today':
        // Start and end are same day
        break;
      case 'this_week':
        // Assuming week starts on Sunday
        const day = start.getDay();
        start.setDate(start.getDate() - day);
        end.setDate(start.getDate() + 6);
        break;
      case 'this_month':
        start.setDate(1);
        end.setMonth(end.getMonth() + 1, 0); // Last day of month
        break;
      case 'past_30_days':
        start.setDate(start.getDate() - 30);
        break;
      case 'ytd':
        start.setMonth(0, 1);
        break;
      case 'past_year':
        start.setFullYear(start.getFullYear() - 1);
        break;
      case 'custom':
        // Do nothing, let user set
        return;
    }

    this.customStart.set(this.formatDate(start));
    this.customEnd.set(this.formatDate(end));
  }

  shiftRange(direction: -1 | 1) {
    const startObj = new Date(this.customStart());
    startObj.setMinutes(startObj.getMinutes() + startObj.getTimezoneOffset()); // Fix tz offset issues

    const endObj = new Date(this.customEnd());
    endObj.setMinutes(endObj.getMinutes() + endObj.getTimezoneOffset());

    const isMonthPreset = this.selectedPreset() === 'this_month';
    const isYearPreset = this.selectedPreset() === 'ytd' || this.selectedPreset() === 'past_year';

    if (isMonthPreset) {
      startObj.setMonth(startObj.getMonth() + direction);
      endObj.setMonth(endObj.getMonth() + direction);
      // Ensure end is last day of the new month
      endObj.setMonth(endObj.getMonth() + 1, 0);
    } else if (isYearPreset) {
      startObj.setFullYear(startObj.getFullYear() + direction);
      endObj.setFullYear(endObj.getFullYear() + direction);
      if (this.selectedPreset() === 'ytd') {
        // Keep YTD behavior but shifted back a year. The end is the end of that year.
        endObj.setMonth(11, 31);
      }
    } else {
      // Day based shifting
      // Calculate duration in days, inclusive
      const diffTime = Math.abs(endObj.getTime() - startObj.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

      const shiftDays = direction * diffDays;
      startObj.setDate(startObj.getDate() + shiftDays);
      endObj.setDate(endObj.getDate() + shiftDays);
    }

    this.selectedPreset.set('custom'); // Once shifted, it might not technically match the preset descriptor perfectly anymore (e.g. "Last Month" isn't "This Month"), so standardizing on 'custom'.
    this.customStart.set(this.formatDate(startObj));
    this.customEnd.set(this.formatDate(endObj));
  }

  private formatDate(d: Date): string {
    const month = '' + (d.getMonth() + 1);
    const day = '' + d.getDate();
    const year = d.getFullYear();

    return [
      year,
      month.padStart(2, '0'),
      day.padStart(2, '0')
    ].join('-');
  }
}
