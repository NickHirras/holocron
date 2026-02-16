# Feature: The Galactic Map (Team Heatmap)

## Strategic Value
Current reports are isolated per team. The "Galactic Map" provides a high-level, visual aggregation of organizational health, allowing leadership to spot burnout or blockers across the entire "galaxy" (organization) at a glance.

## User Story
As a **Director of Engineering**,
I want to view a heatmap of all teams, colored by their latest status (Green/Red/Grey),
So that I can identify which sectors need support without opening every individual report.

## Technical Blueprint
1.  **Service**: Create `HeatmapService`.
    *   Aggregate `CeremonyResponse` data.
    *   Calculate a "Health Score" (0-100) based on participation and blockers.
2.  **Frontend**:
    *   Use CSS Grid or D3.js to render a honeycomb (hex) grid.
    *   Each hex represents a Team.
    *   Color coding:
        *   **Green**: 100% submission, no blockers.
        *   **Red**: Blockers reported.
        *   **Grey**: < 50% submission.
3.  **Interaction**:
    *   Clicking a hex opens the Team Dashboard (existing `/pulse/{teamId}`).
    *   Hovering shows a summary tooltip.

## Acceptance Criteria
- [ ] `/map` route displays all active teams as hex tiles.
- [ ] Tiles are color-coded based on the latest Pulse cycle.
- [ ] "Red" status is triggered immediately if any member reports a blocker.
- [ ] Load time is < 200ms (cached aggregation).
