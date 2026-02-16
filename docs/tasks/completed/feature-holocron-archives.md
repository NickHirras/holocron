# Feature: Holocron Archives

## Strategic Value
The value of a daily standup is not just in the moment, but in the history. "Holocron Archives" unlocks this value by allowing users to browse past blockers, accomplishments, and team history, effectively creating a team knowledge base. 
Phase 1 focuses on the immutable storage and retrieval of these records ("Data Cores"). Phase 2 will introduce deep search capabilities.

## Phase 1: Immutable Records (Completed)

### User Story
As a **Team Lead**,
I want to browse past "Mission Debriefs" (completed pulses),
So that I can review the team's progress and blockers over time without altering historical data.

### Technical Implementation
1.  **Immutable Storage**:
    - `Artifact` entity stores a snapshot of the pulse data as a JSON blob (`summaryJson`).
    - This ensures that even if team structure or questions change, the historical record remains accurate to the time of generation.
2.  **Navigation & UI**:
    - "ARCHIVES" link in the side console.
    - Two-pane layout (`archive.html`): List of past pulses on the left, detailed "Holographic" view on the right.
    - Uses HTMX for seamless "Debrief Reveal" without page reloads.
3.  **Data Generation**:
    - `ArtifactService` aggregates `CeremonyResponse` and `CeremonyAnswer` data into `ArchiveDTO`s.
    - Automated generation upon Pulse closure (simulated via `DevDataSeeder` for now).

### Acceptance Criteria (Phase 1)
- [x] `Artifact` entity created and mapped to `summaryJson`.
- [x] `ArtifactService` correctly aggregates and serializes data.
- [x] Web UI (`/archives`) displays list of past pulses.
- [x] Detail view loads via HTMX.
- [x] Historical data seeding implemented for development.

---

## Phase 2: Search & Discovery (Completed)

### User Story
As a **Developer**,
I want to search for "API migration" to see when we last worked on it and what issues we faced,
So that I don't repeat past mistakes.

### Technical Blueprint
1.  **Database**:
    - Enabled SQLite FTS5 (Full Text Search) extension.
    - Created virtual table `artifacts_fts` using FTS5.
2.  **UI/UX**:
    - Added Global Search input in the Top HUD.
    - Filters Archive results as the user types (`hx-trigger="keyup changed delay:500ms"`).
3.  **Privacy**:
    - Search results respect Team visibility rules.

### Acceptance Criteria (Phase 2)
- [x] Search bar is accessible from the global HUD.
- [x] Results update in real-time (HTMX) or via form submit.
- [x] Clicking a result navigates to the specific historical Pulse.
- [x] Users cannot see results from teams they do not have access to.
