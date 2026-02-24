import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CeremonyClientService } from '../services/ceremony-client';
import { CeremonyResponse, CeremonyTemplate } from '../../proto-gen/holocron/v1/ceremony_pb';

interface ChartData {
    questionId: string;
    title: string;
    type: string;
    answers: any[];
    // Processed data for visualization
    distribution?: { label: string, count: number }[];
    textAnswers?: string[];
}

@Component({
    selector: 'app-ceremony-results',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule],
    templateUrl: './ceremony-results.component.html',
    styleUrl: './ceremony-results.component.scss'
})
export class CeremonyResultsComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private ceremonyClient = inject(CeremonyClientService);

    template = signal<CeremonyTemplate | null>(null);
    responses = signal<CeremonyResponse[]>([]);
    isLoading = signal(true);
    error = signal<string | null>(null);

    startDate = signal<string>('');
    endDate = signal<string>('');

    // Computed signal to aggregate and process data per question
    questionData = computed(() => {
        const tpl = this.template();
        const resps = this.responses();
        if (!tpl || !resps) return [];

        const data: ChartData[] = [];

        // Loop through questions in the template
        for (const item of tpl.items) {
            if (item.kind.case === 'questionItem') {
                const question = item.kind.value.question;
                if (!question) continue;

                const qId = question.questionId;
                const qType = question.type.case || 'unknown';

                const qData: ChartData = {
                    questionId: qId,
                    title: item.title,
                    type: qType,
                    answers: []
                };

                // Gather answers for this question
                for (const resp of resps) {
                    const answer = resp.answers[qId];
                    if (answer) {
                        qData.answers.push(answer);
                    }
                }

                // Process based on type
                if (qType === 'choiceQuestion') {
                    const counts: Record<string, number> = {};
                    qData.answers.forEach(ans => {
                        if (ans.kind.case === 'choiceAnswer') {
                            ans.kind.value.values.forEach((v: string) => {
                                counts[v] = (counts[v] || 0) + 1;
                            });
                        }
                    });
                    qData.distribution = Object.keys(counts).map(k => ({ label: k, count: counts[k] }));
                } else if (qType === 'scaleQuestion') {
                    const counts: Record<string, number> = {};
                    qData.answers.forEach(ans => {
                        if (ans.kind.case === 'scaleAnswer') {
                            const v = ans.kind.value.value.toString();
                            counts[v] = (counts[v] || 0) + 1;
                        }
                    });
                    qData.distribution = Object.keys(counts).map(k => ({ label: k, count: counts[k] })).sort((a, b) => parseInt(a.label) - parseInt(b.label));
                } else if (qType === 'textQuestion') {
                    qData.textAnswers = qData.answers.map(ans => {
                        if (ans.kind.case === 'textAnswer') {
                            return ans.kind.value.value;
                        }
                        return '';
                    }).filter(Boolean);
                }

                data.push(qData);
            }
        }

        return data;
    });

    ngOnInit() {
        this.route.paramMap.subscribe(async params => {
            const id = params.get('id');
            if (id) {
                await this.loadData(id);
            } else {
                this.error.set('No template ID provided');
                this.isLoading.set(false);
            }
        });
    }

    async loadData(templateId: string) {
        this.isLoading.set(true);
        this.error.set(null);
        try {
            const start = this.startDate() ? new Date(this.startDate()) : undefined;
            let end = this.endDate() ? new Date(this.endDate()) : undefined;

            if (end) {
                // Include the entire end day
                end = new Date(end.getTime() + 24 * 60 * 60 * 1000 - 1);
            }

            // Load both template and responses concurrently
            const [templateResp, responsesResp] = await Promise.all([
                this.ceremonyClient.getTemplate(templateId),
                this.ceremonyClient.listResponses(templateId, start, end)
            ]);

            if (templateResp.template) {
                this.template.set(templateResp.template);
            }
            if (responsesResp.responses) {
                this.responses.set(responsesResp.responses);
            }
        } catch (err: any) {
            console.error('Failed to load data', err);
            this.error.set(err?.message || 'Failed to load results');
        } finally {
            this.isLoading.set(false);
        }
    }

    applyDateFilter() {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadData(id);
        }
    }

    downloadCsv() {
        const tpl = this.template();
        const resps = this.responses();
        if (!tpl || !resps || resps.length === 0) return;

        // 1. Create Headers (Question Titles)
        const questions: { id: string, title: string, type: string }[] = [];
        for (const item of tpl.items) {
            if (item.kind.case === 'questionItem') {
                const question = item.kind.value.question;
                if (question) {
                    questions.push({
                        id: question.questionId,
                        title: item.title,
                        type: question.type.case || 'unknown'
                    });
                }
            }
        }

        const headers = ['Response ID', 'Submitted At', ...questions.map(q => `"${q.title.replace(/"/g, '""')}"`)];
        const rows: string[][] = [headers];

        // 2. Create Rows (User Answers)
        for (const resp of resps) {
            const row = [
                resp.responseId,
                resp.submittedAt ? new Date(Number(resp.submittedAt.seconds) * 1000).toISOString() : ''
            ];

            for (const q of questions) {
                const answer = resp.answers[q.id];
                let cellValue = '';

                if (answer) {
                    if (answer.kind.case === 'textAnswer') {
                        cellValue = answer.kind.value.value;
                    } else if (answer.kind.case === 'choiceAnswer') {
                        cellValue = answer.kind.value.values.join(', ');
                    } else if (answer.kind.case === 'scaleAnswer') {
                        cellValue = answer.kind.value.value.toString();
                    }
                    // Handle other types as needed
                }

                // Escape quotes and wrap in quotes for CSV
                row.push(`"${cellValue.replace(/"/g, '""')}"`);
            }
            rows.push(row);
        }

        // 3. Generate and Download
        const csvContent = rows.map(e => e.join(',')).join('\n');
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', `${tpl.title}_results.csv`);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
}
