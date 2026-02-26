import { Injectable } from '@angular/core';
import { customAlphabet } from 'nanoid';
const nanoidGenerator = customAlphabet('23456789abcdefghjkmnpqrstuvwxyz', 12);
import { create } from '@bufbuild/protobuf';
import {
    CeremonyTemplateSchema,
    ItemSchema,
    QuestionItemSchema,
    QuestionSchema,
    TextQuestionSchema,
    ChoiceQuestionSchema,
    ScaleQuestionSchema,
    FileUploadQuestionSchema,
    DateQuestionSchema,
    TimeQuestionSchema,
    OptionSchema,
    ChoiceQuestion_Type,
    PageBreakItemSchema,
    TextItemSchema,
    ImageItemSchema,
    VideoItemSchema,
    NotificationSettingsSchema
} from '../../proto-gen/holocron/v1/ceremony_pb';

@Injectable({
    providedIn: 'root'
})
export class CeremonyMapperService {

    private generateId(): string {
        return nanoidGenerator();
    }

    mapFormToTemplate(formVal: any) {
        const protoItems = formVal.items.map((item: any) => {
            let kindPayload: any = undefined;

            if (item.kind === 'QUESTION') {
                let questionTypePayload: any;

                switch (item.type) {
                    case 'TEXT_SHORT':
                    case 'TEXT_PARAGRAPH':
                        questionTypePayload = {
                            case: 'textQuestion',
                            value: create(TextQuestionSchema, { paragraph: item.type === 'TEXT_PARAGRAPH' })
                        };
                        break;
                    case 'CHOICE_RADIO':
                    case 'CHOICE_CHECKBOX':
                    case 'CHOICE_DROPDOWN':
                        let choiceType = ChoiceQuestion_Type.UNSPECIFIED;
                        if (item.type === 'CHOICE_RADIO') choiceType = ChoiceQuestion_Type.RADIO;
                        if (item.type === 'CHOICE_CHECKBOX') choiceType = ChoiceQuestion_Type.CHECKBOX;
                        if (item.type === 'CHOICE_DROPDOWN') choiceType = ChoiceQuestion_Type.DROP_DOWN;

                        const opts = item.options.map((o: any) => create(OptionSchema, {
                            value: o.value,
                            isOther: o.isOther,
                            nextSectionId: o.nextSectionId
                        }));

                        questionTypePayload = {
                            case: 'choiceQuestion',
                            value: create(ChoiceQuestionSchema, { type: choiceType, options: opts })
                        };
                        break;
                    case 'SCALE':
                        questionTypePayload = {
                            case: 'scaleQuestion',
                            value: create(ScaleQuestionSchema, {
                                low: item.scaleLow,
                                high: item.scaleHigh,
                                lowLabel: item.scaleLowLabel,
                                highLabel: item.scaleHighLabel
                            })
                        };
                        break;
                    case 'FILE_UPLOAD':
                        questionTypePayload = {
                            case: 'fileUploadQuestion',
                            value: create(FileUploadQuestionSchema, {
                                maxFiles: 1,
                                maxFileSizeBytes: 10485760n, // 10MB
                                allowedMimeTypes: []
                            })
                        };
                        break;
                    case 'DATE':
                        questionTypePayload = {
                            case: 'dateQuestion',
                            value: create(DateQuestionSchema, {
                                includeYear: item.includeYear,
                                includeTime: item.includeTime
                            })
                        };
                        break;
                    case 'TIME':
                        questionTypePayload = {
                            case: 'timeQuestion',
                            value: create(TimeQuestionSchema, { duration: item.duration })
                        };
                        break;
                }

                const question = create(QuestionSchema, {
                    questionId: `q_${this.generateId()}`,
                    required: item.required,
                    type: questionTypePayload
                });

                kindPayload = {
                    case: 'questionItem',
                    value: create(QuestionItemSchema, { question })
                };

            } else if (item.kind === 'TEXT') {
                kindPayload = { case: 'textItem', value: create(TextItemSchema, {}) };
            } else if (item.kind === 'SECTION') {
                kindPayload = { case: 'pageBreakItem', value: create(PageBreakItemSchema, { nextSectionId: item.nextSectionId }) };
            } else if (item.kind === 'IMAGE') {
                kindPayload = { case: 'imageItem', value: create(ImageItemSchema, { imageUrl: item.url, altText: item.altText }) };
            } else if (item.kind === 'VIDEO') {
                kindPayload = { case: 'videoItem', value: create(VideoItemSchema, { videoUrl: item.url, caption: item.altText }) };
            }

            return create(ItemSchema, {
                itemId: `item_${this.generateId()}`,
                title: item.title,
                description: item.description,
                kind: kindPayload
            });
        });

        const notificationSettings = create(NotificationSettingsSchema, {
            webhookUrls: formVal.webhookUrls ? formVal.webhookUrls.split(',').map((u: string) => u.trim()).filter((u: string) => u) : [],
            emailAddresses: formVal.emailNotifications ? formVal.emailNotifications.split(',').map((e: string) => e.trim()).filter((e: string) => e) : []
        });

        return create(CeremonyTemplateSchema, {
            title: formVal.title,
            description: formVal.description,
            teamId: formVal.teamId || '',
            collectEmails: formVal.collectEmails,
            limitOneResponse: formVal.limitOneResponse,
            shuffleQuestionOrder: formVal.shuffleQuestionOrder,
            confirmationMessage: formVal.confirmationMessage,
            isPublic: formVal.isPublic,
            sharedWithEmails: formVal.sharedWithEmails ? formVal.sharedWithEmails.split(',').map((e: string) => e.trim()).filter((e: string) => e) : [],
            notificationSettings: notificationSettings,
            items: protoItems
        });
    }
}
