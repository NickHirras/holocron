# Feature Task: Ceremony UX Redesign

> **Status**: Planned
> **Theme**: UX & Tooling
> **Priority**: High

## Context
The current experience for creating ceremonies and managing questions is suboptimal. Users expect a smooth, drag-and-drop interface similar to "Google Forms" for managing lists of questions and configuring ceremony options.

## Objective
Implement a "Ceremony Builder" interface that allows for intuitive creation, editing, and reordering of ceremonies and their associated questions.

## Implementation Plan

### 1. Backend: Ceremony & Question Management
- [ ] **Ceremony Endpoints**: Ensure `CeremonyController` supports:
    - `POST /ceremonies`: Create new ceremony.
    - `PUT /ceremonies/{id}`: Update specific fields (title, rrule, team).
- [ ] **Question Endpoints**: Ensure `CeremonyQuestionResource` supports:
    - `POST /ceremonies/{id}/questions`: Add a new question to a ceremony.
    - `PUT /questions/{id}`: Update individual question fields (text, type, required).
    - `DELETE /questions/{id}`: Remove a question.
    - `POST /ceremonies/{id}/questions/reorder`: Accept a list of question IDs in their new order and update `sequence`. **MUST be `@Transactional`.**

### 2. Frontend: The "Ceremony Builder"
- [ ] **New Route**: `/ceremonies/builder/{id}` (or new create at `/ceremonies/new`).
- [ ] **Layout**:
    - **Header**: Editable Ceremony Title and Description.
    - **Settings Sidebar/Panel**: RRule scheduler, Team selector, Pulse check-in active toggle.
    - **Questions Canvas**: The main area.
- [ ] **Interactivity (HTMX + SortableJS)**:
    - **Drag & Drop**: Use `SortableJS` on the question list. On `end` event, trigger HTMX request to `/reorder` endpoint.
    - **Accessibility**: key-based reordering (Up/Down buttons) for non-mouse users.
    - **Inline Editing**: Clicking text turns it into an input field (or use clean input fields that look like text). Auto-save on blur (`hx-trigger="change delay:500ms"`, `hx-post="..."`).
    - **Dynamic Types**: Changing the question type (Text, Scale, Boolean) immediately updates the visual representation (e.g., showing 1-5 radio buttons for scale).
    - **Add Question**: Floating action button or bottom card to append a new question template.
- [ ] **Assets**:
    - Download `SortableJS` to `src/main/resources/META-INF/resources/assets/js/` (do not use CDN).

### 3. Deliverables
- [ ] `CeremonyBuilderController` (or existing `CeremonyController` updates).
- [ ] `builder.html` Qute template.
- [ ] `_question_card.html` Qute fragment (for HTMX swaps).
- [ ] Integration of locally served `SortableJS`.

## UX Considerations
- **"Google Forms" Feel**: Clean cards, distinct shadows on hover, smooth transitions.
- **Feedback**:  "Saving..." / "Saved" global indicator triggered by HTMX events.
- **Empty States**: Clear CTA when no questions exist.
- **Accessibility**: Ensure keyboard navigation works for all interactive elements, especially reordering.

## Acceptance Criteria
- [ ] User can create a new Ceremony and land immediately in the builder.
- [ ] User can drag and drop questions to reorder them; change persists on reload.
- [ ] **Accessibility**: User can reorder questions using Up/Down buttons.
- [ ] User can change question text and type without page reloads.
- [ ] **Reliability**: Network failures during reordering revert the UI state or show an error.
- [ ] Layout follows the Holocron aesthetic (Glassmorphism/Star Wars) but prioritizes usability for this complex form.
