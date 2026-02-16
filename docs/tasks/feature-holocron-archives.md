# Feature: Holocron Archives (Search)

## Strategic Value
The value of a daily standup is not just in the moment, but in the history. "Holocron Archives" unlocks this value by allowing users to search past blockers, accomplishments, and team history, effectively creating a team knowledge base.

## User Story
As a **Developer**,
I want to search for "API migration" to see when we last worked on it and what issues we faced,
So that I don't repeat past mistakes.

## Technical Blueprint
1.  **Database**:
    *   Enable SQLite FTS5 (Full Text Search) extension (if supported by driver) or use `LIKE` queries for MVP.
    *   Create a virtual table `responses_search` if using FTS5.
2.  **UI/UX**:
    *   Add a Global Search input in the Top HUD.
    *   Create `/archives` page for results.
    *   Use HTMX to filter results as the user types (`hx-trigger="keyup changed delay:500ms"`).
3.  **Privacy**:
    *   Search results must respect Team visibility rules.

## Acceptance Criteria
- [ ] Search bar is accessible from the global HUD.
- [ ] Results update in real-time (HTMX) or via form submit.
- [ ] Clicking a result navigates to the specific historical Pulse.
- [ ] Users cannot see results from teams they do not have access to.
