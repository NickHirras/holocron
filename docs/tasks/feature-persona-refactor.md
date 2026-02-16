# Feature: Persona & UI Refactor (Friction Removal)

## Strategic Value
The current `dashboard.html` relies heavily on complex inline logic (`{#if ...}`) to render different states ("Transmission Sent", "Incoming", etc.). This makes it hard to maintain the "Alive Hologram" persona consistently. Refactoring these into reusable Qute fragments (or separate components) will improve code readability and allow for richer, isolated animations.

## User Story
As a **Developer**,
I want the dashboard states to be managed by small, testable UI fragments,
So that I can tweak the "glitch" animations without breaking the logic.

## Technical Blueprint
1.  **Qute Fragments**:
    *   Create `src/main/resources/templates/components/status_card.html`.
    *   Create `src/main/resources/templates/components/leader_stats.html`.
2.  **Animation Integration**:
    *   Move the `blink` and `glitch` CSS classes into `holocron.css` (if not already fully centralized).
    *   Ensure fragments have stable IDs for HTMX updates.
3.  **Refactor**:
    *   Simplify `dashboard.html` to include these fragments.

## Acceptance Criteria
- [ ] `dashboard.html` file size is reduced by > 30%.
- [ ] "Status Card" logic is isolated in a reusable component.
- [ ] Visual regression test: The dashboard looks exactly the same (or better) to the user.
