# Architecture

## Dynamic Ceremony Scheduling

The "Next Pulse" Pattern:

1.  A **Ceremony** is defined with an RRule.
2.  When the app starts (or a schedule changes), use a **Quarkus Startup Observer** to find the `nextOccurrence` for all active Ceremonies using `lib-recur`.
3.  Programmatically schedule a one-time trigger using the **Quarkus Scheduler API** (`io.quarkus.scheduler.Scheduler`).
4.  Upon execution, the job performs the ceremony and calculates the next date to re-schedule itself.

### Time & Scheduling Policy
*   **Calculations:** Use `lib-recur` to expand RRules in-memory. **Do not store future occurrences as individual DB rows.**
*   **Zone Logic:** Perform all schedule expansions in the `Team.timezone_id`.
*   **Display:** Convert `ZonedDateTime` to the individual User's browser/profile timezone *only* at the UI layer.
*   **Dynamic Triggers:** Use `Quarkus Startup Observer` to calculate the next "Pulse" for all active ceremonies and schedule them via the `Quarkus Scheduler API`.

## Time Zones
**Time Zone Policy:** All RRule calculations must be performed in the Team's local TimeZone, but stored in the database with a reference to that specific Zone ID (e.g., `America/New_York`).

## Data Integrity & Security

### SQLite Performance
Must use **WAL (Write-Ahead Logging) mode** and `synchronous = NORMAL` for optimal concurrent performance.

### Auditability
All mutation operations (Create/Update/Delete) on core entities (Teams, Questionnaires) must be logged to an `AuditEntry` table.

## Notifications

*   **Providers:** Support for Slack, Microsoft Teams, and Email.
*   **Strategy:** See [NOTIFICATIONS.md](NOTIFICATIONS.md) for detailed use cases and urgency levels.

## Domain Terminology

*   **Ceremony:** The high-level event (e.g., "Daily Standup").
*   **Pulse:** A specific occurrence of a Ceremony (e.g., "Standup for Feb 11").
*   **Artifact:** The output of a Pulse (e.g., a PDF summary or a list of action items).
