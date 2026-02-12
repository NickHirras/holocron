# Holocron UX Flows

## 1. Authentication ("Identity Verification")

The entry point to the system must feel like accessing a secure terminal.

### Flow Step-by-Step
1.  **Landing Page**:
    *   User arrives at `/`.
    *   **Visual**: A rotating Holocron cube.
    *   **Action**: User clicks "INITIALIZE PROTOCOL" (Login).
2.  **OAuth Handoff**:
    *   Standard redirect to Google/GitHub.
    *   *Note*: We can't style the provider pages, but we can style the "Callback" page.
3.  **Identity Verification (Callback)**:
    *   User returns to `/callback`.
    *   **Visual**: A "BIOS Loading" screen. Text scrolls rapidly: `> DECRYPTING CREDENTIALS...`, `> MATCHING BIO-SIGNATURE...`, `> ACCESS GRANTED`.
    *   **Transition**: Screen "wipes" vertically to reveal the Dashboard.

## 2. Daily Standup ("Reporting In")

This is the most frequent action. It must be frictionless but engaging.

### Flow Step-by-Step
1.  **Dashboard Trigger**:
    *   User sees a "MISSION PENDING: DAILY STANDUP" alert on the dashboard.
    *   Status indicator is flashing Amber.
    *   **Action**: Click "BEGIN TRANSMISSION".
2.  **The Interface (Ceremony Mode)**:
    *   The dashboard fades out. The "Ceremony Interface" slides in.
    *   It is a focussed, single-column view.
3.  **Question Sequence**:
    *   **Q1**: "Report: Previous Cycle" (What did you do?).
        *   User types. *Sound*: Subtle key clicks.
        *   Press `Enter` or `Tab` to next.
    *   **Q2**: "Mission Objectives: Current Cycle" (What will you do?).
    *   **Q3**: "Obstructions Detected?" (Blockers).
        *   Toggle switch [NO / YES].
        *   If YES -> Input field expands with a glitch effect.
4.  **Submission**:
    *   Final step shows a summary.
    *   **Action**: Click "TRANSMIT DATA".
    *   **Feedback**: A "Uploading..." bar fills up. A positive chime plays.
    *   **Result**: "TRANSMISSION COMPLETE". User is returned to Dashboard.
    *   **State Change**: Dashboard status is now Green ("Secure").

## 3. Commander View ("Overseer Deck")

For managers/leads to review team progress.

### Flow Step-by-Step
1.  **Access**:
    *   From Dashboard, click "SECTOR CONTROL" (Team View).
2.  **The Grid**:
    *   View a grid of all team members.
    *   **States**:
        *   *Green Border*: Submitted, no blockers.
        *   *Red Border + Pulse*: Blocked.
        *   *Grey / Dim*: Not submitted.
3.  **Drill Down**:
    *   Clicking a member card expands their "Hologram" (Details view).
    *   Shows their latest entry and historical stats.

## 4. Navigation Model
*   **Sidebar/Navbar**: hidden by default? Or a "Dock" at the bottom?
*   **Proposal**: Top "HUD" bar for global status (User, Connection, Time). Side "Panel" for navigation that slides out on hover.

