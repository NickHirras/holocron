# Tech Stack

## 1. Core Framework & Runtime
*   **Framework:** Quarkus 3.x+ ("Supersonic Subatomic Java")
*   **Java Version:** Java 25 LTS (utilizing Virtual Threads for high concurrency)
*   **Build Tool:** Gradle 9.3.1 (Kotlin DSL recommended)
*   **Rendering Engine:** Qute Templating Engine
    *   **Type:** Server-Side Rendering (SSR).
    *   **Features:** Reflection-free, type-safe templates, asynchronous by default.
    *   **Location:** Templates reside in `src/main/resources/templates`.

## 2. Persistence Layer
*   **Database:** SQLite
*   **Driver:** `quarkus-jdbc-sqlite`
*   **Connection:** Local file-based storage (e.g., `jdbc:sqlite:data/application.db`).
*   **ORM / Data Access:** Hibernate ORM with Panache
    *   **Pattern:** Active Record or Repository pattern.
    *   **Optimization:** Uses blocking JDBC (standard for SQLite) but optimized via Virtual Threads.
*   **Schema Migrations:** Flyway
    *   **Location:** `src/main/resources/db/migration/`
    *   **Strategy:** `V<Version>__<Description>.sql` scripts executed automatically on startup.

### Scheduling
*   **Recurrence Engine:** `lib-recur` (`org.dmfs:lib-recur`).
*   **Storage Pattern:** Store RFC 5545 RRule strings in the Schedule entity.
*   **Constraint:** Do not store future occurrences as individual rows; expand them in-memory using `RecurrenceRuleIterator` for dashboard views and logic.
*   **SQLite Optimization:** WAL Mode must be enabled.

## 3. Frontend & UI
*   **CSS Framework:** Pico CSS v2 (Classless)
    *   **Approach:** Semantic HTML styling. Minimal custom classes.
    *   **Theme:** Automatic light/dark mode support based on system preferences.
*   **Interactivity:** HTMX
    *   **Role:** AJAX-driven DOM updates via HTML fragments returned from the server.
    *   **Constraint:** No heavy SPA frameworks (React/Angular). Maintain logic in Java/Qute.
*   **Assets:** Managed via Quarkus Web Bundler or simple static files in `META-INF/resources`.

## 4. Containerization & Deployment
*   **Image Builder:** Google Jib (`quarkus-container-image-jib`)
    *   **Workflow:** Builds optimized, layered Docker images directly from the build tool without a Dockerfile.
*   **Runtime:** JVM (HotSpot) for most use cases; GraalVM Native Image for ultra-low memory footprints.
*   **Storage Requirement:** SQLite databases must be stored on a persistent volume (e.g., `/data`) to ensure data persistence across container restarts.

## 5. Caching Layer

Stick with **Caffeine**. It’s already built into Quarkus, requires zero extra infrastructure (no extra Docker containers), and provides the lowest possible latency for your RRule calculations.

* **Primary Cache: Caffeine** (Quarkus-integrated).
* **Strategy:** > * Use `@CacheResult` on RRule expansion methods.
   * Use `@CacheInvalidate` when a Ceremony's schedule is updated.
* **Template Caching:** Qute template definitions are automatically cached by the engine for high-speed rendering.

## Auth
*   **Implementation:** Local user/pass for now.
*   **Future:** Support oauth providers optionally.
