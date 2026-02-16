# Feature Task: Council of Ranks (Gamification)

> **Status**: Planned
> **Theme**: Gamification
> **Priority**: Medium (Big Bet)

## Objective
Implement gamification mechanics ("Pulse Streaks", "Council Ranks", "XP") to drive daily retention and make consistency satisfying.

## Tasks
- [ ] **Schema Design**
    - [ ] Create `UserStats` entity (or extend `User`) to track:
        - `currentStreak` (int)
        - `longestStreak` (int)
        - `lastPulseDate` (LocalDate)
        - `totalXp` (long)
    - [ ] Create migration script.
- [ ] **Backend Logic**
    - [ ] Implement `StreakService` to calculate streaks on submission.
    - [ ] Add "Streak Protection" logic (optional - e.g., weekends don't break streaks).
    - [ ] Create `LevelUpService` to handle rank promotion based on XP.
- [ ] **UI Integration**
    - [ ] Display "Current Streak" on the Dashboard.
    - [ ] Show a "Streak Increased!" notification after Pulse submission.
    - [ ] Design badges for different ranks (Padawan, Knight, Master).

## Acceptance Criteria
- [ ] Submitting a Pulse increment user's streak.
- [ ] Missing a day resets the streak (unless protected).
- [ ] Users can see their current rank and progress to the next rank.
