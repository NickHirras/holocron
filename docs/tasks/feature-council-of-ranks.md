# Feature Task: Council of Ranks (Gamification)

> **Status**: In Review / Verifying
> **Theme**: Gamification
> **Priority**: Medium (Big Bet)

## Recent Verification (2026-02-16)
- [x] **Browser Verification**: Dashboard and Pulse pages validated for multiple personas (Tarkin, Vader).
- [x] **Bug Fixes**:
    - Resolved `TransactionRequiredException` on Pulse page.
    - Fixed `TransientObjectException` for new users accessing Dashboard.
    - Corrected Overseer page layout glitch (recursive nesting).
    - Fixed Schema Validation startup error.

## Objective
Implement gamification mechanics ("Pulse Streaks", "Council Ranks", "XP") to drive daily retention and make consistency satisfying.

## Tasks
- [x] **Schema Design**
    - [x] Create `UserStats` entity (or extend `User`) to track:
        - `currentStreak` (int)
        - `longestStreak` (int)
        - `lastPulseDate` (LocalDate)
        - `totalXp` (long)
    - [x] Create migration script.
- [x] **Backend Logic**
    - [x] Implement `StreakService` to calculate streaks on submission.
    - [ ] Add "Streak Protection" logic (optional - e.g., weekends don't break streaks).
    - [ ] Create `LevelUpService` to handle rank promotion based on XP.
- [x] **UI Integration**
    - [x] **Dashboard Integration**: "Council Rank" widget (Streak, XP, Rank Badge).
    - [x] **Holo-Glow Effect**: Define CSS for streak multiplier glow.
    - [x] **Interaction Flow**: "Happy Path" for Pulse submission (HTMX fragments).
    - [x] **Cognitive Load Audit**: Minimalist "Cockpit" feel.
    - [x] **Visual FX**: Scrolling terminal animation for debrief.

## Acceptance Criteria
- [x] Submitting a Pulse increment user's streak.
- [x] Missing a day resets the streak (unless protected).
- [x] Users can see their current rank and progress to the next rank.
