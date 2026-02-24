import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
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
    imports: [CommonModule, RouterModule],
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
            // Load both template and responses concurrently
            const [templateResp, responsesResp] = await Promise.all([
                this.ceremonyClient.getTemplate(templateId),
                this.ceremonyClient.listResponses(templateId)
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
}
