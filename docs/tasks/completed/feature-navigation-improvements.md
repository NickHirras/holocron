# Feature: Navigation Improvements (Friction Removal)

## Strategic Value
During friction discovery, it was noted that Team Leads have a hard time finding the "Overseer Deck" or managing their team directly from the Dashboard. This feature bridges that gap, ensuring that "Sector Control" is always one click away for authorized personnel.

## User Story
As a **Team Lead**,
I want a clear "Manage Sector" button on my dashboard card,
So that I don't have to hunt for the "Sector Command" link in the bottom console.

## Technical Blueprint
1.  **Dashboard Template**:
    *   Update `dashboard.html`.
    *   Inject `identity` or `user` role checks into the "Sector Info" or "Main Status" card.
    *   Add a secondary action button: `Manage Team` pointing to `/overseer`.
2.  **Console Layout**:
    *   Ensure the "SECTOR COMMAND" link in `base.html` is highlighted or pulsed if there are pending actions (e.g., blocked team members).

## Acceptance Criteria
- [x] Team Leads see a "Manage Team" button on the Dashboard.
- [x] Regular users DO NOT see this button.
- [x] The "Sector Command" link in the bottom console is verified to work for Leads.

## Implementation Details
- **Side Panel**: Replaced bottom console with a slide-out side panel (`components/side_panel.html`) triggered by a hot-zone.
- **HTMX**: Enabled partial navigation for Dashboard and Profile to prevent full page reloads.
- **Global HUD**: Added status hex and system time to `base.html` header.
- **Cleanup**: Removed legacy bottom navigation code.
