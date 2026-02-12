# Testing Strategy

## Philosophy
We value **User Journeys** over excessive unit isolation. Since Holocron is an HTMX application, the "Unit" is often the HTTP Request/Response cycle, including the rendered HTML.

## 1. Pyramid of Testing

### ▲ End-to-End (E2E) - *Playwright*
**Scope:** Critical User Journeys that *must* work.
**Tool:** Playwright (Java or Node.js binding TBD, likely Node for ecosystem support).
**Execution:** Runs in CI against the containerized application.

**Critical Journeys:**
1.  **Authentication**: Login via OIDC (Mocked in CI) -> Redirect to Dashboard.
2.  **Ceremony Lifecycle**: Create Team -> Create Standup -> Submit Standup.
3.  **Scheduling**: Verify "Next Pulse" calculation updates after submission.

### ◼ Integration - *QuarkusTest*
**Scope:** HTTP Endpoints, Database Constraints, RRule Logic.
**Tool:** `@QuarkusTest` + `RestAssured`.
**Data:** Uses an in-memory SQLite DB or a temporary file DB.

### ▼ Unit - *JUnit 5*
**Scope:** Pure business logic (e.g., complex RRule expansion helpers, text parsing).
**Constraint:** No I/O, no Spring/Quarkus context. Fast execution.

## 2. CI/CD Integration
*   **Pull Requests**: Run Unit + Integration Tests.
*   **Pre-Release (Tag)**: Run Full E2E Suite with Playwright.

## 3. Load Testing
*   **Goal:** Verify SQLite WAL mode handling under concurrent write load (simulating 50 people submitting standups at 9:00 AM).
*   **Threshold:** p99 latency < 200ms.
