# Feature: Protocol Droids (Dynamic Ceremony Scheduling)

## Strategic Value
Currently, ceremonies are static entities. This feature implements the core promise of the Holocron architecture: dynamic, timezone-aware scheduling. It ensures that "Pulse" events are generated automatically based on RRules, removing manual intervention and supporting global teams.

## User Story
As a **Team Lead**,
I want my team's "Daily Standup" to appear in their dashboard automatically at 09:00 AM (local time),
So that we can maintain our rhythm without manual triggers.

## Technical Blueprint
1.  **Dependency**: Add `quarkus-scheduler` and `lib-recur` (or compatible RRule library) to `pom.xml`.
2.  **Service**: Create `ScheduleService`.
    *   On startup (`@Observes StartupEvent`), scan all active `Ceremony` entities.
    *   For each ceremony, calculate `nextOccurrence` using the Team's timezone.
3.  **Scheduler**: Programmatically schedule a job for `nextOccurrence`.
    *   Job logic: Create a `Pulse`, send notifications, and re-schedule the next job.
4.  **Handling Missed Pulses**: If the server was down, the startup observer should detect if a pulse was missed and trigger it immediately (or skip, based on policy).

## Acceptance Criteria
- [ ] Application schedules jobs for all active ceremonies on startup.
- [ ] "Pulse" records are created automatically at the correct time.
- [ ] RRules respect the Team's `timezoneId`.
- [ ] Server restart preserves or recalculates the schedule without duplicate pulses.
