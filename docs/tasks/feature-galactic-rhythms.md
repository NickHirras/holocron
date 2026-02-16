# Feature: Galactic Rhythms (Ceremony & Pulse Management)

> **Status**: Planned  
> **Theme**: Core Infrastructure  
> **Priority**: High (Big Bet)

## ⚡ Strategic Value
Holocron's mission is to eliminate engineering friction through structured interactions. Currently, team ceremonies are largely static or manually seeded. This feature empowers Team Leads to define custom "Rituals"—such as Architecture Reviews or Incident Post-mortems—and automates the "Pulse" lifecycle using RRule scheduling to support global, async engineering teams.

## 👤 User Stories
- **As a Team Lead**, I want to create a new Ceremony with a custom frequency (e.g., Daily M-F at 09:00) so that my team maintains a consistent rhythm.
- **As a Team Lead**, I want to define specific questions (Text, Scale, Boolean, etc.) for each ceremony to capture the exact data my team needs.
- **As a Developer**, I want to see upcoming Pulses on my "Galactic Map" so I know when my next mission report is due.

## 🛠️ Technical Blueprint

### 1. Data Model & Integrity
- **Ceremony Refinement**: Fully utilize `Ceremony.rrule` (RFC 5545) and `Ceremony.timezone` for accurate scheduling.
- **Audit Trail**: Every creation or modification of a Ceremony must be logged to the `audit_entries` table per project security protocols.

### 2. "Protocol Droid" Scheduling Logic
- **Startup Observation**: Use a `Quarkus Startup Observer` to calculate the `nextOccurrence` for all active ceremonies using `lib-recur`.
- **In-Memory Expansion**: Adhere to the "Next Pulse" pattern: do not store future occurrences as individual DB rows.
- **Caching**: Utilize **Caffeine** to cache recurrence calculations for high-speed UI rendering.

### 3. Management UI (Sector Command)
- **Ceremony Builder**: A multi-step form to initialize ceremony parameters (Title, Description, RRule).
- **Questionnaire Designer**: An HTMX-powered interface to add, remove, and reorder `CeremonyQuestion` entities.
- **Manual Overrides**: Provide Team Leads with "Emergency Trigger" capabilities to manually initialize a Pulse outside of the schedule.

## Still to-do:
- [ ] Implement the "Questionnaire Designer" interactive features (drag-and-drop reordering) fully (currently read-only listing with placeholder add button).
- [ ] Connect the `ScheduleService` to a real StartupEvent that loads all active ceremonies (logic is present but needs full integration testing with real time/scheduler).


## 🎯 Acceptance Criteria
- [ ] Team Leads can create, edit, and terminate Ceremonies for their assigned Sectors.
- [ ] Support for varied question types: `TEXT`, `SCALE`, `BOOLEAN`, `INTEGER`, and `SELECTION`.
- [ ] The `ScheduleService` successfully automates `CeremonyResponse` placeholder creation at specified intervals.
- [ ] All management actions are captured in the `AuditEntry` table.
- [ ] UI maintains the "Alive Hologram" aesthetic with chamfered edges and monospaced data.

---
*"Do or do not. There is no try."*