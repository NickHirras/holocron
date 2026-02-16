# Feature Task: The Overseer Deck (Management Dashboard)

> **Status**: Planned
> **Theme**: Management
> **Priority**: Medium (Big Bet)

## Objective
Create a grid-view dashboard for Team Leads to visualize team health, submission status, and blockers at a glance.

## Tasks
- [ ] **Data Aggregation**
    - [ ] Create `TeamHealthDTO` to aggregate:
        - Team members.
        - Submission status for today.
        - Blocker status.
        - Sentiment score (if available).
    - [ ] Optimize queries to avoid N+1 issues.
- [ ] **UI Implementation**
    - [ ] Create `overseer.html` template.
    - [ ] Implement "Grid View" of user cards.
    - [ ] Add visual indicators for status:
        - Green Border: Submitted, OK.
        - Red Pulse: Blocked.
        - Grey: Not submitted.
    - [ ] Add "Expand" functionality to see individual user's latest response.
- [ ] **Access Control**
    - [ ] Ensure only users with `role = 'LEAD'` or similar can access this view.

## Acceptance Criteria
- [ ] Leads can see all their team members on one screen.
- [ ] Blockers are immediately visible (red/pulsing).
- [ ] Clicking a member shows their latest update details.
