# Holocron Decision Log 📜

This document records the architectural decisions made for the Holocron project to maintain consistency and technical integrity.

## 001: Java 25 & Virtual Threads
* **Date**: 2026-02-15
* **Decision**: Use Java 25 LTS as the primary runtime.
* **Context**: We prioritize high concurrency for ceremony submissions.
* **Consequence**: We favor simple, blocking-style JDBC code over complex reactive patterns, as Virtual Threads handle the I/O blocking efficiently.

## 002: SQLite in WAL Mode
* **Date**: 2026-02-15
* **Decision**: Use SQLite with Write-Ahead Logging (WAL) and `synchronous = NORMAL`.
* **Context**: We need a local, file-based database that supports concurrent readers and writers for remote team syncs.
* **Consequence**: Avoids "Database is locked" errors during high-load periods (e.g., 9:00 AM Standup Storms).

## 003: HTMX + Pico CSS (Server-Side First)
* **Date**: 2026-02-15
* **Decision**: Use HTMX for interactivity and Pico CSS for styling.
* **Context**: We want a "Holographic" terminal feel without the overhead of a heavy SPA framework like React.
* **Consequence**: UI logic is kept in Qute templates, and partial page updates are handled via HTML fragments.

## 004: In-Memory RRule Expansion
* **Date**: 2026-02-15
* **Decision**: Use `lib-recur` to expand ceremony schedules in-memory rather than storing future occurrences in the DB.
* **Context**: Reduces database bloat and allows for dynamic schedule changes.
* **Consequence**: We utilize Caffeine for high-speed caching of these calculations.

## 005: Offline-First Development Mode
* **Date**: 2026-02-15
* **Decision**: Implement a custom `DevAuthenticationMechanism` and `DevDataSeeder`.
* **Context**: Engineers should be able to build the future of ceremonies without an active internet connection or OIDC provider.
* **Consequence**: Mock identities (Alice, Bob, etc.) are used to simulate various team roles instantly.