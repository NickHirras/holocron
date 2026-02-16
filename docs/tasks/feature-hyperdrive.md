# Feature: Hyperdrive (SQLite WAL & Async)

## Strategic Value
As the dataset grows, the default SQLite locking mechanism will become a bottleneck. "Hyperdrive" ensures the database can handle concurrent reads/writes without locking the UI, and offloads heavy aggregation tasks to Virtual Threads to keep the application responsive.

## User Story
As a **System Administrator**,
I want the database to operate in WAL mode,
So that background log writes don't block user entry submissions.

## Technical Blueprint
1.  **Configuration**:
    *   Update `application.properties`: `quarkus.datasource.jdbc.url=jdbc:sqlite:data/holocron.db?journal_mode=WAL&busy_timeout=5000`.
    *   Set `synchronous=NORMAL` for performance/durability balance.
2.  **Concurrency**:
    *   Refactor `OverseerService` and `HeatmapService` methods to use `@RunOnVirtualThread` (if blocking) or standard reactive patterns.
    *   Ensure creating `Pulse` records (Scheduler) doesn't block HTTP reads.
3.  **Testing**:
    *   Load test with `jmeter` or `k6` to verify no `SQLITE_BUSY` errors under load.

## Acceptance Criteria
- [ ] Database file is accompanied by `-wal` and `-shm` files during runtime.
- [ ] Concurrent read/write stress test passes with 0 failures to acquire lock.
- [ ] `application.properties` explicitly defines WAL mode.
