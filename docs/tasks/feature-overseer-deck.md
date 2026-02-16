# Feature Task: The Overseer Deck (Management Dashboard)

> **Status**: Completed
> **Theme**: Management
> **Priority**: Medium (Big Bet)

## Objective
Create a grid-view dashboard for Team Leads to visualize team health, submission status, and blockers at a glance.

## Tasks
- [x] **Data Aggregation**
    - [x] Create `TeamHealthDTO` to aggregate:
        - Team members.
        - Submission status for today.
        - Blocker status.
        - Sentiment score (if available).
    - [x] Optimize queries to avoid N+1 issues.
- [x] **UI Implementation**
    - [x] Create `overseer.html` template.
    - [x] Implement "Grid View" of user cards.
    - [x] Add visual indicators for status:
        - Green Border: Submitted, OK.
        - Red Pulse: Blocked.
        - Grey: Not submitted.
    - [x] Add "Expand" functionality to see individual user's latest response.
- [x] **Access Control**
    - [x] Ensure only users with `role = 'LEAD'` or similar can access this view.

## Acceptance Criteria
- [x] Leads can see all their team members on one screen.
- [x] Blockers are immediately visible (red/pulsing).
- [x] Clicking a member shows their latest update details.
