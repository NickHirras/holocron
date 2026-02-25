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

interface CrossTabData {
    groupLabel: string;
    groupCount: number;
    distribution: { label: string, count: number }[];
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

    // Cross-Tabulation State
    crossTabGroupQuestionId = signal<string>('');
    crossTabTargetQuestionId = signal<string>('');

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

    // Helper signal for dropdowns (only choices and scales)
    selectableQuestions = computed(() => {
        return this.questionData().filter(q => q.type === 'choiceQuestion' || q.type === 'scaleQuestion');
    });

    // Cross-tabulation compute logic
    crossTabData = computed(() => {
        const groupQId = this.crossTabGroupQuestionId();
        const targetQId = this.crossTabTargetQuestionId();
        const resps = this.responses();

        if (!groupQId || !targetQId || !resps || resps.length === 0) return null;

        const results = new Map<string, { count: number, dist: Map<string, number> }>();

        for (const resp of resps) {
            const groupAns = resp.answers[groupQId];
            const targetAns = resp.answers[targetQId];

            if (!groupAns || !targetAns) continue;

            // Extract string value for grouping
            let groupValue = 'Unknown';
            if (groupAns.kind.case === 'choiceAnswer') {
                groupValue = groupAns.kind.value.values.join(', ');
            } else if (groupAns.kind.case === 'scaleAnswer') {
                groupValue = groupAns.kind.value.value.toString();
            }

            // Extract string value(s) for target distribution targeting
            let targetValues: string[] = [];
            if (targetAns.kind.case === 'choiceAnswer') {
                targetValues = targetAns.kind.value.values;
            } else if (targetAns.kind.case === 'scaleAnswer') {
                targetValues = [targetAns.kind.value.value.toString()];
            }

            if (targetValues.length === 0) continue;

            // Initialize group if not exists
            if (!results.has(groupValue)) {
                results.set(groupValue, { count: 0, dist: new Map() });
            }

            const groupData = results.get(groupValue)!;
            groupData.count++;

            for (const val of targetValues) {
                const currentCount = groupData.dist.get(val) || 0;
                groupData.dist.set(val, currentCount + 1);
            }
        }

        // Convert Maps to sorted Arrays for UI
        const finalData: CrossTabData[] = [];
        for (const [groupLabel, data] of results.entries()) {
            const distribution = Array.from(data.dist.entries())
                .map(([label, count]) => ({ label, count }))
                // Basic sort for labels like '1', '2' or 'Yes', 'No'
                .sort((a, b) => a.label.localeCompare(b.label, undefined, { numeric: true }));

            finalData.push({
                groupLabel,
                groupCount: data.count,
                distribution
            });
        }

        // Sort groups alphabetically/numerically
        return finalData.sort((a, b) => a.groupLabel.localeCompare(b.groupLabel, undefined, { numeric: true }));
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

    downloadCrossTabCsv() {
        const tpl = this.template();
        const data = this.crossTabData();
        const groupQId = this.crossTabGroupQuestionId();
        const targetQId = this.crossTabTargetQuestionId();

        if (!tpl || !data || !groupQId || !targetQId) return;

        // Find titles for headers
        let groupTitle = 'Group';
        let targetTitle = 'Target';
        for (const item of tpl.items) {
            if (item.kind.case === 'questionItem' && item.kind.value.question) {
                if (item.kind.value.question.questionId === groupQId) groupTitle = item.title;
                if (item.kind.value.question.questionId === targetQId) targetTitle = item.title;
            }
        }

        // We need all possible target distribution labels to create columns
        const allTargetLabels = new Set<string>();
        data.forEach(group => {
            group.distribution.forEach(d => allTargetLabels.add(d.label));
        });

        // Sort labels
        const sortedTargetLabels = Array.from(allTargetLabels).sort((a, b) => a.localeCompare(b, undefined, { numeric: true }));

        const headers = [`"${groupTitle.replace(/"/g, '""')}"`, 'Total Responses', ...sortedTargetLabels.map(l => `"${l.replace(/"/g, '""')}"`)];
        const rows: string[][] = [headers];

        data.forEach(group => {
            const row = [
                `"${group.groupLabel.replace(/"/g, '""')}"`,
                group.groupCount.toString()
            ];

            // Map distribution to sorted target labels
            const distMap = new Map(group.distribution.map(d => [d.label, d.count]));
            for (const label of sortedTargetLabels) {
                row.push((distMap.get(label) || 0).toString());
            }

            rows.push(row);
        });

        // 3. Generate and Download
        const csvContent = rows.map(e => e.join(',')).join('\n');
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        // Replace spaces with underscores for filename safely
        const safeGroupTitle = groupTitle.replace(/[^a-z0-9]/gi, '_').toLowerCase();
        const safeTargetTitle = targetTitle.replace(/[^a-z0-9]/gi, '_').toLowerCase();

        link.setAttribute('download', `${tpl.title}_crosstab_${safeGroupTitle}_vs_${safeTargetTitle}.csv`);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
}
