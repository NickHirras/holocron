# Project Holocron: Session Handoff

## Current State: Ceremony Creator (Phase 1)
We have successfully implemented the foundation for the Ceremony Creator. The UI correctly renders a Reactive Form (with a dark premium aesthetic), uses ConnectRPC to pack form values into a `ceremony.proto` `CeremonyTemplate`, sends it to the Armeria backend on port `8080`, and persists it to MongoDB using the Coroutine driver.

*Present Capabilities:*
- Dynamic `FormArray` structure.
- Only supports `TextQuestion` items (both short-answer and paragraph boolean).

## Objective for Next Session: Complex Question Types & Google Forms Parity
The core objective for the next session is to expand the Form Builder UI and the underlying data model to achieve parity with Google Forms.

### 1. Data Model Updates (`holocron/v1/ceremony.proto`)
Our data model already supports:
- `TextQuestion`, `ChoiceQuestion` (Radio, Checkbox, Dropdown), `ScaleQuestion` (Linear), `FileUploadQuestion`.
- It also supports structural items: `QuestionGroupItem` (Multiple Choice Grid), `PageBreakItem` (Section), `TextItem` (Title/Desc), `ImageItem`, `VideoItem`.

**Missing Data Model Features (Action Required):**
- **Date Question:** Need to add `DateQuestion` to the `Question.type` oneof (include year, include time).
- **Time Question:** Need to add `TimeQuestion` to the oneof (duration vs time of day).
- **Section Navigation:** We need a way to support logic branching (e.g., "Go to section 2 based on answer" on specific `Option`s within a `ChoiceQuestion`). Look into adding a `string next_section_id` field to `Option` and `PageBreakItem`.
- **Form Settings:** We may want form-level configurations (collect emails, limit to 1 response, shuffle question order, confirmation message) currently missing from `CeremonyTemplate`.

### 2. Frontend Angular UI Implementation (`ceremony-creator.ts` / `.html`)
The `CeremonyCreatorComponent` is currently rigged specifically for `TextQuestion`. To support dynamic types, we need:

**Step 2a: Question Type Selector**
- A dropdown (similar to Google Forms) inside each question card to pick the type: Short Answer, Paragraph, Multiple Choice, Checkboxes, Dropdown, File Upload, Linear Scale, Date, Time.

**Step 2b: Dynamic Form Arrays (Nested)**
- When a user selects a `ChoiceQuestion` (Multiple Choice / Checkbox / Dropdown), the UI must present an "Options List."
- This requires a *nested* `FormArray` inside the question's `FormGroup`. Users must be able to add/remove options, and toggle the "Add 'Other'" option.

**Step 2c: Polymorphic Sub-Forms**
- **Linear Scale (`ScaleQuestion`)**: Inputs to set the range bounds (e.g. 1 to 5, 0 to 10) and the low/high text labels.
- **File Upload (`FileUploadQuestion`)**: Inputs to set allowed types (PDF, Images, etc.), max files, and max file size.
- **Date/Time**: Checkboxes for "Include Year", "Include Time" or "Duration".

**Step 2d: Non-Question Structural Items**
- Toolbars/FABs to insert items other than Questions: "Add Title and Description" (`TextItem`), "Add Image" (`ImageItem`), "Add Video" (`VideoItem`), "Add Section" (`PageBreakItem`).

**Step 2e: Reordering (Drag and Drop)**
- Using Angular CDK `@angular/cdk/drag-drop` to reorder the root `items` FormArray, ensuring `item_id` sequencing matches user intent.

**Step 2f: Protobuf Serialization Logic**
- Update the `saveTemplate()` method to properly branch on the `formVal` item types and instantiate the correct `ceremony_pb.ts` objects (e.g., `create(ChoiceQuestionSchema, ...)`).

## Next Session "Golden Loop"
1. Update `proto/holocron/v1/ceremony.proto` with the missing Question types (Date, Time).
2. Run `make gen` from the root directory.
3. Update `CeremonyCreatorComponent` to support selecting question types and rendering nested option arrays.
4. Verify the serialization mapping in `saveTemplate()`. 
5. Test End-to-End via the browser subagent.
