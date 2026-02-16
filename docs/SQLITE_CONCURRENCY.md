# SQLite Concurrency & Integrity

## ⚙️ Configuration
Holocron utilizes SQLite in **Write-Ahead Logging (WAL)** mode with `synchronous = NORMAL` to enable concurrent readers and a single writer without performance degradation.

## 🛡️ Implementation Rules
1. **Synchronous Mode**: Set to `NORMAL` to balance safety and performance.
2. **Transaction Scoping**: Always use `@Transactional` for mutation operations to ensure data integrity.
3. **Auditability**: Every mutation (Create/Update/Delete) must log an entry to the `AuditEntry` table.
4. **Virtual Threads**: Since SQLite JDBC is blocking, we leverage Java 25 Virtual Threads to prevent the underlying OS threads from being pinned during I/O.

## 🛡️ Engineering Rules
1. **Virtual Thread Awareness**: Since SQLite JDBC is blocking, we rely on **Java 25 Virtual Threads** to handle I/O without pinning OS threads. Keep database logic simple and sequential.
2. **Atomic Mutations**: All CUD (Create, Update, Delete) operations must be wrapped in `@Transactional` to ensure the integrity of the Sith Archives (the database).
3. **Audit Trail**: Every mutation must be logged to an `AuditEntry` table for administrative review.
4. **In-Memory Scheduling**: Do **not** persist future ceremony occurrences as database rows. Expand RRules in-memory using `lib-recur` and cache via **Caffeine**.

## 📊 High-Load Scenario
Tests must simulate "Standup Storms" (50+ concurrent users submitting at once). p99 latency must stay below 200ms.

## 📊 Scheduling Policy
Do **not** store future occurrences of ceremonies as individual rows. Use `lib-recur` to expand RRules in-memory during request time and cache the results using **Caffeine**.
