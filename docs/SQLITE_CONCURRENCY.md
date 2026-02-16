# SQLite Concurrency & Integrity

## ⚙️ Configuration
Holocron utilizes SQLite in **Write-Ahead Logging (WAL)** mode. This allows multiple readers and one writer concurrently.

## 🛡️ Implementation Rules
1. **Synchronous Mode**: Set to `NORMAL` to balance safety and performance.
2. **Transaction Scoping**: Always use `@Transactional` for mutation operations to ensure data integrity.
3. **Auditability**: Every mutation (Create/Update/Delete) must log an entry to the `AuditEntry` table.
4. **Virtual Threads**: Since SQLite JDBC is blocking, we leverage Java 25 Virtual Threads to prevent the underlying OS threads from being pinned during I/O.

## 📊 Scheduling Policy
Do **not** store future occurrences of ceremonies as individual rows. Use `lib-recur` to expand RRules in-memory during request time and cache the results using **Caffeine**.