import { Component, inject, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { DragDropModule, CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { CeremonyClientService } from '../services/ceremony-client';
import { CeremonyMapperService } from '../services/ceremony-mapper.service';
import { ImageUploaderComponent } from '../components/image-uploader/image-uploader.component';

@Component({
  selector: 'app-ceremony-creator',
  imports: [CommonModule, ReactiveFormsModule, RouterModule, DragDropModule, ImageUploaderComponent],
  templateUrl: './ceremony-creator.html',
  styleUrl: './ceremony-creator.scss',
})
export class CeremonyCreator implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private ceremonyClient = inject(CeremonyClientService);
  private ceremonyMapper = inject(CeremonyMapperService);
  private destroyRef = inject(DestroyRef);

  templateForm!: FormGroup;
  isSubmitting = false;

  questionTypes = [
    { value: 'TEXT_SHORT', label: 'Short answer', icon: 'short_text' },
    { value: 'TEXT_PARAGRAPH', label: 'Paragraph', icon: 'subject' },
    { value: 'CHOICE_RADIO', label: 'Multiple choice', icon: 'radio_button_checked' },
    { value: 'CHOICE_CHECKBOX', label: 'Checkboxes', icon: 'check_box' },
    { value: 'CHOICE_DROPDOWN', label: 'Dropdown', icon: 'arrow_drop_down_circle' },
    { value: 'FILE_UPLOAD', label: 'File upload', icon: 'cloud_upload' },
    { value: 'SCALE', label: 'Linear scale', icon: 'linear_scale' },
    { value: 'DATE', label: 'Date', icon: 'event' },
    { value: 'TIME', label: 'Time', icon: 'schedule' }
  ];

  ngOnInit() {
    this.templateForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      teamId: [''],
      // Form Settings
      collectEmails: [false],
      limitOneResponse: [false],
      shuffleQuestionOrder: [false],
      confirmationMessage: ['Your response has been recorded.'],
      isPublic: [false],
      sharedWithEmails: [''],
      // Integrations
      webhookUrls: [''],
      emailNotifications: [''],
      items: this.fb.array([])
    });

    this.route.queryParams.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      const type = params['type'];
      const teamId = params['teamId'];

      if (teamId) {
        this.templateForm.patchValue({ teamId });
      }

      if (type === 'standup') {
        this.prefillStandup();
      } else if (type === 'retro') {
        this.prefillRetro();
      } else {
        // Start with one empty question if no template
        this.addQuestion('TEXT_SHORT');
      }
    });
  }

  get items(): FormArray {
    return this.templateForm.get('items') as FormArray;
  }

  getOptions(itemIndex: number): FormArray {
    return this.items.at(itemIndex).get('options') as FormArray;
  }

  createQuestionGroup(type: string = 'TEXT_SHORT'): FormGroup {
    const group = this.fb.group({
      kind: ['QUESTION'], // QUESTION, TEXT, SECTION, IMAGE, VIDEO
      title: ['', Validators.required],
      description: [''],
      required: [false],
      type: [type], // Specific question type

      // Choice specifics
      options: this.fb.array([]),

      // Scale specifics
      scaleLow: [1],
      scaleHigh: [5],
      scaleLowLabel: [''],
      scaleHighLabel: [''],

      // Date/Time specifics
      includeYear: [true],
      includeTime: [false],
      duration: [false],

      // Media/Section specifics
      url: [''],
      altText: [''],
      nextSectionId: ['']
    });

    if (type.startsWith('CHOICE_')) {
      (group.get('options') as FormArray).push(this.createOption());
    }

    // React to type changes
    group.get('type')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(newType => {
      // Initialize options array if switching to a choice type and it's empty
      if (newType?.startsWith('CHOICE_')) {
        const optionsArray = group.get('options') as FormArray;
        if (optionsArray.length === 0) {
          optionsArray.push(this.createOption());
        }
      }
    });

    return group;
  }

  createOption(): FormGroup {
    return this.fb.group({
      value: ['Option 1', Validators.required],
      isOther: [false],
      nextSectionId: ['']
    });
  }

  addQuestion(type: string = 'TEXT_SHORT', title: string = '', desc: string = '') {
    const group = this.createQuestionGroup(type);
    group.patchValue({ title, description: desc });
    this.items.push(group);
  }

  addStructuralItem(kind: string) { // TEXT, SECTION, IMAGE, VIDEO
    const group = this.createQuestionGroup('TEXT_SHORT');
    group.patchValue({ kind: kind, title: `New ${kind}`, url: '' });
    this.items.push(group);
  }

  addOption(itemIndex: number) {
    const options = this.getOptions(itemIndex);
    const newOption = this.createOption();
    newOption.patchValue({ value: `Option ${options.length + 1}` });
    options.push(newOption);
  }

  addOtherOption(itemIndex: number) {
    const options = this.getOptions(itemIndex);
    const hasOther = options.controls.some(ctrl => ctrl.value.isOther);
    if (!hasOther) {
      const newOption = this.createOption();
      newOption.patchValue({ value: 'Other', isOther: true });
      options.push(newOption);
    }
  }

  removeOption(itemIndex: number, optionIndex: number) {
    this.getOptions(itemIndex).removeAt(optionIndex);
  }

  removeItem(index: number) {
    this.items.removeAt(index);
  }

  dropItem(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.items.controls, event.previousIndex, event.currentIndex);
    this.items.updateValueAndValidity();
  }

  private prefillStandup() {
    this.templateForm.patchValue({
      title: 'Daily Standup',
      description: 'Quick sync to share progress and highlight blockers.'
    });
    this.addQuestion('TEXT_PARAGRAPH', 'What did you accomplish yesterday?', '');
    this.addQuestion('TEXT_PARAGRAPH', 'What are you working on today?', '');
    this.addQuestion('TEXT_PARAGRAPH', 'Are there any blockers?', 'List anything preventing progress.');
  }

  private prefillRetro() {
    this.templateForm.patchValue({
      title: 'Sprint Retrospective',
      description: 'Reflect on the past sprint to improve future performance.'
    });
    this.addQuestion('TEXT_PARAGRAPH', 'What went well?', 'Mad/Sad/Glad or similar format.');
    this.addQuestion('TEXT_PARAGRAPH', 'What could be improved?', '');
    this.addQuestion('TEXT_PARAGRAPH', 'Action Items', 'What will we do differently next sprint?');
  }
  async saveTemplate() {
    if (this.templateForm.invalid) {
      this.templateForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formVal = this.templateForm.value;

    try {
      const template = this.ceremonyMapper.mapFormToTemplate(formVal);

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
