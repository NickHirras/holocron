# Feature Task: Council of Ranks (Gamification)

> **Status**: Planned
> **Theme**: Gamification
> **Priority**: Medium (Big Bet)

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
- [ ] **UI Integration**
    - [ ] **Dashboard Integration**: "Council Rank" widget (Streak, XP, Rank Badge).
    - [ ] **Holo-Glow Effect**: Define CSS for streak multiplier glow.
    - [ ] **Interaction Flow**: "Happy Path" for Pulse submission (HTMX fragments).
    - [ ] **Cognitive Load Audit**: Minimalist "Cockpit" feel.
    - [ ] **Visual FX**: Scrolling terminal animation for debrief.

## Acceptance Criteria
- [x] Submitting a Pulse increment user's streak.
- [x] Missing a day resets the streak (unless protected).
- [ ] Users can see their current rank and progress to the next rank.
