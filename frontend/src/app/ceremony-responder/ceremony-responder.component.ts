import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray, FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CeremonyClientService } from '../services/ceremony-client';
import { CeremonyTemplate, Item, CeremonyResponseSchema, AnswerSchema, TextAnswerSchema, ChoiceAnswerSchema, ScaleAnswerSchema, DateAnswerSchema, TimeAnswerSchema } from '../../proto-gen/holocron/v1/ceremony_pb';
import { create } from '@bufbuild/protobuf';

@Component({
    selector: 'app-ceremony-responder',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './ceremony-responder.component.html',
    styleUrls: ['./ceremony-responder.component.scss']
})
export class CeremonyResponderComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private ceremonyClient = inject(CeremonyClientService);
    private fb = inject(FormBuilder);

    template = signal<CeremonyTemplate | null>(null);
    formGroup: FormGroup = this.fb.group({});

    loading = signal<boolean>(true);
    error = signal<string | null>(null);
    submitting = signal<boolean>(false);
    submitted = signal<boolean>(false);

    // Group items by pages based on PageBreakItem
    pages = signal<Item[][]>([]);
    currentPageIndex = signal<number>(0);

    ngOnInit() {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadTemplate(id);
        } else {
            this.error.set('No ceremony ID provided.');
            this.loading.set(false);
        }
    }

    async loadTemplate(id: string) {
        try {
            this.loading.set(true);
            const response = await this.ceremonyClient.getTemplate(id);
            if (response.template) {
                this.template.set(response.template);
                this.buildForm(response.template);
                this.buildPages(response.template);
            } else {
                this.error.set('Template not found.');
            }
        } catch (err: any) {
            this.error.set('Error loading template: ' + err.message);
        } finally {
            this.loading.set(false);
        }
    }

    buildPages(template: CeremonyTemplate) {
        const p: Item[][] = [];
        let currentPage: Item[] = [];

        for (const item of template.items) {
            if (item.kind.case === 'pageBreakItem') {
                p.push(currentPage);
                currentPage = [item]; // Start next page with the break (to hold logic)
            } else {
                currentPage.push(item);
            }
        }
        if (currentPage.length > 0) {
            p.push(currentPage);
        }

        // If the first item on the first page is a page break (unlikely but possible), it's fine.
        // Usually, the first page has no page break item.
        this.pages.set(p);
    }

    buildForm(template: CeremonyTemplate) {
        const group: any = {};

        for (const item of template.items) {
            if (item.kind.case === 'questionItem') {
                const q = item.kind.value.question;
                if (!q) continue;

                const validators = q.required ? [Validators.required] : [];

                if (q.type.case === 'choiceQuestion' && q.type.value.type === 2) {
                    // Checkbox needs an array
                    group[q.questionId] = this.fb.array([], validators);
                } else if (q.type.case === 'dateQuestion') {
                    // Provide a subgroup for date fields
                    group[q.questionId] = this.fb.group({
                        date: ['', validators],
                        time: ['']
                    });
                } else if (q.type.case === 'timeQuestion') {
                    group[q.questionId] = ['', validators];
                } else {
                    // Default single control
                    group[q.questionId] = ['', validators];
                }

                // Handle "Other" inputs dynamically if checked/selected
                if (q.type.case === 'choiceQuestion') {
                    group[`${q.questionId}_other`] = [''];
                }
            } else if (item.kind.case === 'questionGroupItem') {
                // Handling grids
                item.kind.value.questions.forEach(q => {
                    const validators = q.required ? [Validators.required] : [];
                    group[q.questionId] = ['', validators];
                });
            }
        }

        this.formGroup = this.fb.group(group);
    }

    onCheckboxChange(e: any, questionId: string) {
        const checkArray: FormArray = this.formGroup.get(questionId) as FormArray;
        if (e.target.checked) {
            checkArray.push(new FormControl(e.target.value));
        } else {
            let i: number = 0;
            checkArray.controls.forEach((item: any) => {
                if (item.value == e.target.value) {
                    checkArray.removeAt(i);
                    return;
                }
                i++;
            });
        }
    }

    isCheckboxChecked(questionId: string, value: string): boolean {
        const checkArray: FormArray = this.formGroup.get(questionId) as FormArray;
        return checkArray?.value.includes(value);
    }

    nextPage() {
        const current = this.pages()[this.currentPageIndex()];
        // Find if the current page has a page break at the start to dictate next logic,
        // or if a selected choice overrides the flow.
        // For MVP next logic, we simply go to the next index, unless overridden.

        // Check if any question on this page has a choice that dictates next_section_id
        let nextSectionId: string | undefined;

        for (const item of current) {
            if (item.kind.case === 'questionItem' && item.kind.value.question?.type.case === 'choiceQuestion') {
                const qId = item.kind.value.question.questionId;
                const val = this.formGroup.get(qId)?.value;

                if (val) {
                    const option = item.kind.value.question.type.value.options.find(o => o.value === val);
                    if (option && option.nextSectionId) {
                        nextSectionId = option.nextSectionId;
                        break;
                    }
                }
            }
        }

        // If no choice override, maybe the page break dictates it?
        // Not fully implemented in this MVP, defaulting to +1

        if (nextSectionId) {
            // Find page index where the first item's ID matches nextSectionId or title matches
            // In Google Forms, sections are usually PageBreakItems.
        }

        if (this.currentPageIndex() < this.pages().length - 1) {
            this.currentPageIndex.update(i => i + 1);
            window.scrollTo(0, 0);
        }
    }

    previousPage() {
        if (this.currentPageIndex() > 0) {
            this.currentPageIndex.update(i => i - 1);
            window.scrollTo(0, 0);
        }
    }

    async onSubmit() {
        if (this.formGroup.invalid) {
            this.formGroup.markAllAsTouched();
            return;
        }

        const t = this.template();
        if (!t) return;

        this.submitting.set(true);

        try {
            const response = create(CeremonyResponseSchema, {
                ceremonyTemplateId: t.id,
                // user_id and submitted_at handled by backend
                answers: {}
            });

            for (const item of t.items) {
                if (item.kind.case === 'questionItem') {
                    const q = item.kind.value.question;
                    if (!q) continue;

                    const val = this.formGroup.get(q.questionId)?.value;
                    // Skip if empty and not required
                    if ((val === '' || val === null || (Array.isArray(val) && val.length === 0)) && !q.required) continue;

                    let answerKind: any;

                    switch (q.type.case) {
                        case 'textQuestion':
                            answerKind = { case: 'textAnswer', value: create(TextAnswerSchema, { value: val || '' }) };
                            break;
                        case 'choiceQuestion':
                            let finalValues = Array.isArray(val) ? val : [val];
                            // Handle "Other"
                            const otherVal = this.formGroup.get(`${q.questionId}_other`)?.value;
                            if (otherVal && finalValues.includes('__other__')) {
                                finalValues = finalValues.map((v: string) => v === '__other__' ? otherVal : v);
                            }
                            answerKind = { case: 'choiceAnswer', value: create(ChoiceAnswerSchema, { values: finalValues }) };
                            break;
                        case 'scaleQuestion':
                            answerKind = { case: 'scaleAnswer', value: create(ScaleAnswerSchema, { value: parseInt(val, 10) || 0 }) };
                            break;
                        case 'dateQuestion':
                            if (val.date) {
                                const d = new Date(val.date);
                                answerKind = {
                                    case: 'dateAnswer', value: create(DateAnswerSchema, {
                                        year: d.getUTCFullYear(),
                                        month: d.getUTCMonth() + 1,
                                        day: d.getUTCDate()
                                    })
                                };
                            }
                            break;
                        case 'timeQuestion':
                            if (val) {
                                const [h, m] = val.split(':');
                                answerKind = {
                                    case: 'timeAnswer', value: create(TimeAnswerSchema, {
                                        hours: parseInt(h, 10) || 0,
                                        minutes: parseInt(m, 10) || 0
                                    })
                                };
                            }
                            break;
                    }

                    if (answerKind) {
                        response.answers[q.questionId] = create(AnswerSchema, { kind: answerKind });
                    }
                }
            }

            await this.ceremonyClient.submitResponse(response);
            this.submitted.set(true);

        } catch (err: any) {
            this.error.set('Failed to submit: ' + err.message);
        } finally {
            this.submitting.set(false);
        }
    }

    // Type Helpers for the template
    getChoiceQuestion(item: Item) {
        if (item.kind.case === 'questionItem' && item.kind.value.question?.type.case === 'choiceQuestion') {
            return item.kind.value.question.type.value;
        }
        return null;
    }

    getTextQuestion(item: Item) {
        if (item.kind.case === 'questionItem' && item.kind.value.question?.type.case === 'textQuestion') {
            return item.kind.value.question.type.value;
        }
        return null;
    }

    getScaleQuestion(item: Item) {
        if (item.kind.case === 'questionItem' && item.kind.value.question?.type.case === 'scaleQuestion') {
            return item.kind.value.question.type.value;
        }
        return null;
    }

    getDateQuestion(item: Item) {
        if (item.kind.case === 'questionItem' && item.kind.value.question?.type.case === 'dateQuestion') {
            return item.kind.value.question.type.value;
        }
        return null;
    }
}
