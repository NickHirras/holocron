const fs = require('fs');

const content = fs.readFileSync('src/app/ceremony-creator/ceremony-creator.html', 'utf8');

let startIndex = content.indexOf('<!-- Scale Type -->');
let endIndex = content.indexOf('<!-- Floating Action Bar (Inline for now) -->');

let replacementText = `<!-- Scale Type -->
                        @if (itemCtrl.value.type === 'SCALE') {
                        <div class="flex items-center gap-4 mt-4">
                            <select formControlName="scaleLow"
                                class="bg-slate-800/50 border border-slate-600 rounded text-slate-300 py-1.5 px-3">
                                <option [ngValue]="0">0</option>
                                <option [ngValue]="1">1</option>
                            </select>
                            <span class="text-slate-500">to</span>
                            <select formControlName="scaleHigh"
                                class="bg-slate-800/50 border border-slate-600 rounded text-slate-300 py-1.5 px-3">
                                @for (n of [2,3,4,5,6,7,8,9,10]; track n) {
                                <option [ngValue]="n">{{n}}</option>
                                }
                            </select>
                        </div>
                        <div class="mt-6 space-y-3 max-w-sm">
                            <div class="flex items-center gap-3">
                                <span class="text-slate-400 w-4">{{ itemCtrl.value.scaleLow }}</span>
                                <input type="text" formControlName="scaleLowLabel" placeholder="Label (optional)"
                                    class="bg-transparent border-b border-slate-700 focus:border-holocron-neon-blue text-sm text-white focus:outline-none w-full transition-colors">
                            </div>
                            <div class="flex items-center gap-3">
                                <span class="text-slate-400 w-4">{{ itemCtrl.value.scaleHigh }}</span>
                                <input type="text" formControlName="scaleHighLabel" placeholder="Label (optional)"
                                    class="bg-transparent border-b border-slate-700 focus:border-holocron-neon-blue text-sm text-white focus:outline-none w-full transition-colors">
                            </div>
                        </div>
                        }

                        <!-- Date / Time -->
                        @if (itemCtrl.value.type === 'DATE') {
                        <div class="flex gap-4">
                            <label class="flex items-center gap-2 text-sm text-slate-300">
                                <input type="checkbox" formControlName="includeYear"
                                    class="rounded bg-slate-800 border-slate-600 text-holocron-neon-blue focus:ring-holocron-neon-blue">
                                Include year
                            </label>
                            <label class="flex items-center gap-2 text-sm text-slate-300">
                                <input type="checkbox" formControlName="includeTime"
                                    class="rounded bg-slate-800 border-slate-600 text-holocron-neon-blue focus:ring-holocron-neon-blue">
                                Include time
                            </label>
                        </div>
                        }
                        @if (itemCtrl.value.type === 'TIME') {
                        <div class="flex gap-4">
                            <label class="flex items-center gap-2 text-sm text-slate-300">
                                <input type="checkbox" formControlName="duration"
                                    class="rounded bg-slate-800 border-slate-600 text-holocron-neon-blue focus:ring-holocron-neon-blue">
                                Duration format
                            </label>
                        </div>
                        }
                        }
                    </div>

                    @if (itemCtrl.value.kind === 'IMAGE') {
                    <div class="mt-4 relative group/image max-w-2xl mx-auto">
                        <app-image-uploader [imageUrl]="itemCtrl.value.url"
                            (imageUrlChange)="itemCtrl.get('url')?.setValue($event)"></app-image-uploader>
                    </div>
                    }
                </div>

                <!-- Card Actions (Bottom Right) -->
                <div class="mt-8 pt-4 border-t border-slate-700/50 flex justify-end items-center gap-4">
                    <button type="button" (click)="removeItem(i)"
                        class="text-slate-400 hover:text-red-400 transition-colors p-2" title="Delete">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2"
                            stroke="currentColor" class="w-5 h-5">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0" />
                        </svg>
                    </button>
                    @if (itemCtrl.value.kind === 'QUESTION') {
                    <div class="h-6 w-px bg-slate-700"></div>
                    <label class="flex items-center gap-2 text-sm text-slate-300 font-medium">
                        Required
                        <input type="checkbox" formControlName="required"
                            class="w-10 h-5 rounded-full bg-slate-700 border-none appearance-none focus:ring-0 focus:outline-none checked:bg-holocron-neon-blue transition-colors cursor-pointer relative after:content-[''] after:absolute after:top-0.5 after:left-0.5 after:bg-white after:w-4 after:h-4 after:rounded-full checked:after:translate-x-5 after:transition-transform">
                    </label>
                    }
                </div>
            </div>
            }
        </div>
        </form>

    `;

let newContent = content.substring(0, startIndex) + replacementText + content.substring(endIndex);

fs.writeFileSync('src/app/ceremony-creator/ceremony-creator.html', newContent);
console.log('Fixed file.');
