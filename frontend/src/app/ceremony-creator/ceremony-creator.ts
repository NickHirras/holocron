import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { CeremonyClientService } from '../services/ceremony-client';
import { create } from '@bufbuild/protobuf';
import {
  CeremonyTemplateSchema,
  ItemSchema,
  QuestionItemSchema,
  QuestionSchema,
  TextQuestionSchema
} from '../../proto-gen/holocron/v1/ceremony_pb';

@Component({
  selector: 'app-ceremony-creator',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './ceremony-creator.html',
  styleUrl: './ceremony-creator.scss',
})
export class CeremonyCreator implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private ceremonyClient = inject(CeremonyClientService);

  templateForm!: FormGroup;
  isSubmitting = false;

  ngOnInit() {
    this.templateForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      items: this.fb.array([])
    });

    this.route.queryParams.subscribe(params => {
      const type = params['type'];
      if (type === 'standup') {
        this.prefillStandup();
      } else if (type === 'retro') {
        this.prefillRetro();
      }
    });
  }

  get items(): FormArray {
    return this.templateForm.get('items') as FormArray;
  }

  addTextQuestion(title: string = '', desc: string = '', paragraph: boolean = true) {
    const itemGroup = this.fb.group({
      title: [title, Validators.required],
      description: [desc],
      paragraph: [paragraph] // simple mock for TextQuestion type
    });
    this.items.push(itemGroup);
  }

  removeQuestion(index: number) {
    this.items.removeAt(index);
  }

  private prefillStandup() {
    this.templateForm.patchValue({
      title: 'Daily Standup',
      description: 'Quick sync to share progress and highlight blockers.'
    });
    this.addTextQuestion('What did you accomplish yesterday?', '', true);
    this.addTextQuestion('What are you working on today?', '', true);
    this.addTextQuestion('Are there any blockers?', 'List anything preventing progress.', true);
  }

  private prefillRetro() {
    this.templateForm.patchValue({
      title: 'Sprint Retrospective',
      description: 'Reflect on the past sprint to improve future performance.'
    });
    this.addTextQuestion('What went well?', 'Mad/Sad/Glad or similar format.', true);
    this.addTextQuestion('What could be improved?', '', true);
    this.addTextQuestion('Action Items', 'What will we do differently next sprint?', true);
  }

  generateId(): string {
    return Math.random().toString(36).substring(2, 10);
  }

  async saveTemplate() {
    if (this.templateForm.invalid) {
      this.templateForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formVal = this.templateForm.value;

    try {
      // Build Protobuf objects from the form
      const protoItems = formVal.items.map((item: any, index: number) => {

        const textQ = create(TextQuestionSchema, {
          paragraph: item.paragraph
        });

        const question = create(QuestionSchema, {
          questionId: `q_${this.generateId()}`,
          required: true,
          type: { case: 'textQuestion', value: textQ }
        });

        const qItem = create(QuestionItemSchema, {
          question: question
        });

        return create(ItemSchema, {
          itemId: `item_${this.generateId()}`,
          title: item.title,
          description: item.description,
          kind: { case: 'questionItem', value: qItem }
        });
      });

      const template = create(CeremonyTemplateSchema, {
        title: formVal.title,
        description: formVal.description,
        items: protoItems
      });

      console.log('Sending template to backend:', template);
      const resp = await this.ceremonyClient.createTemplate(template);
      console.log('Successfully saved template:', resp.template);

      this.router.navigate(['/dashboard']);
    } catch (err: any) {
      console.error('Failed to save template:', err);
      alert('Failed to save template. See console for details.');
    } finally {
      this.isSubmitting = false;
    }
  }
}
