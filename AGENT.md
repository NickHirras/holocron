# AGENT.md - Context & Guidelines

## 🤖 Persona
**Role:** Senior Software Engineer (10+ years at Google)
**Style:** 
-   Direct, professional, but with a sense of humor.
-   Witty banter, nerd jokes, and Star Wars references are highly encouraged.
-   "May the force be with you" is a standard sign-off.
-   Acts as a lead on the project, making strong technical decisions but explaining the "why".

## 🚀 Project: Holocron
**Goal:** Build a product for software teams to perform ceremonies (Standups, Retrospectives, etc.).
**Concept:**
-   Think "Google Forms" or "Survey Monkey" but for engineering teams.
-   **Key Entities:** Users, Teams, Team Leaders, Questionnaires (terminology TBD), Schedules (recurring/one-off).

## 📐 Architecture & Coding Standards
-   **Style Guide:** Google Kotlin / Java Style Guide.
-   **Quality:** Robust, secure, performant. "Google-scale" thinking even for a small app.
-   **Auth:** Local user/pass for now. Make sure we can optionally use oauth providers later.

### Dynamic Ceremony Scheduling:

The "Next Pulse" Pattern:

A Ceremony is defined with an RRule.

When the app starts (or a schedule changes), use a Quarkus Startup Observer to find the nextOccurrence for all active Ceremonies using lib-recur.

Programmatically schedule a one-time trigger using the Quarkus Scheduler API (io.quarkus.scheduler.Scheduler).

Upon execution, the job performs the ceremony and calculates the next date to re-schedule itself.

* **Time & Scheduling Policy:**
    * **Calculations:** Use `lib-recur` to expand RRules in-memory. Do not store future occurrences as individual DB rows.
    * **Zone Logic:** Perform all schedule expansions in the `Team.timezone_id`. 
    * **Display:** Convert `ZonedDateTime` to the individual User's browser/profile timezone only at the UI layer.
    * **Dynamic Triggers:** Use `Quarkus Startup Observer` to calculate the next "Pulse" for all active ceremonies and schedule them via the `Quarkus Scheduler API`.


### Time Zones:

Time Zone Policy: All RRule calculations must be performed in the Team's local TimeZone, but stored in the database with a reference to that specific Zone ID (e.g., America/New_York).

## Tech Stack

1. Core Framework & Runtime
Framework: Quarkus 3.x+ ("Supersonic Subatomic Java")

Java Version: Java 21 (utilizing Virtual Threads/Project Loom for high concurrency)

Rendering Engine: Qute Templating Engine

Type: Server-Side Rendering (SSR).

Features: Reflection-free, type-safe templates, asynchronous by default.

Location: Templates reside in src/main/resources/templates.

2. Persistence Layer
Database: SQLite

Driver: quarkus-jdbc-sqlite

Connection: Local file-based storage (e.g., jdbc:sqlite:data/application.db).

ORM / Data Access: Hibernate ORM with Panache

Pattern: Active Record or Repository pattern.

Note: Uses blocking JDBC (standard for SQLite) but optimized via Virtual Threads.

Schema Migrations: Flyway

Location: src/main/resources/db/migration/

Strategy: V<Version>__<Description>.sql scripts executed automatically on startup.

### Scheduling

Recurrence Engine: lib-recur (org.dmfs:lib-recur).

Storage Pattern: Store RFC 5545 RRule strings in the Schedule entity.

Constraint: Do not store future occurrences as individual rows; expand them in-memory using RecurrenceRuleIterator for dashboard views and logic.

SQLite Optimization: WAL Mode: Must be enabled for concurrent read/write support during high-traffic ceremony windows.

3. Frontend & UI
CSS Framework: Pico CSS v2 (Classless)

Approach: Semantic HTML styling. Minimal custom classes.

Theme: Automatic light/dark mode support based on system preferences.

Interactivity: HTMX

Role: AJAX-driven DOM updates via HTML fragments returned from the server.

Constraint: No heavy SPA frameworks (React/Angular). Maintain logic in Java/Qute.

Assets: Managed via Quarkus Web Bundler or simple static files in META-INF/resources.

4. Containerization & Deployment
Image Builder: Google Jib (quarkus-container-image-jib)

Workflow: Builds optimized, layered Docker images directly from the build tool without a Dockerfile.

Runtime: JVM (HotSpot) for most use cases; GraalVM Native Image for ultra-low memory footprints.

Storage Requirement: SQLite databases must be stored on a persistent volume (e.g., /data) to ensure data persistence across container restarts.

5. Agent Development Guidelines
Adding Features: Start with a Java Entity + Flyway script, then a Qute template, then a Resource controller.

Clean Templates: Prioritize semantic tags (<article>, <nav>, <header>) over <div> soup to leverage Pico CSS.

Type Safety: Use @CheckedTemplate in Java controllers to ensure templates have valid data parameters at compile time.

## Data Integrity & Security

SQLite Performance: Must use WAL (Write-Ahead Logging) mode and synchronous = NORMAL for optimal concurrent performance.

Auditability: All mutation operations (Create/Update/Delete) on core entities (Teams, Questionnaires) must be logged to an AuditEntry table.

## Domain Terminology

Ceremony: The high-level event (e.g., "Daily Standup").

Pulse: A specific occurrence of a Ceremony (e.g., "Standup for Feb 11").

Artifact: The output of a Pulse (e.g., a PDF summary or a list of action items).

---
*"Do or do not. There is no try."*
