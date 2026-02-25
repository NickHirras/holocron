# Holocron Security Audit: OWASP Top 10 (2021)

This document provides a high-level security audit of the Holocron application (Version 1.0) mapped against the OWASP Top 10 (2021) standard, highlighting how the architecture inherently protects against common vulnerabilities.

## 1. Broken Access Control
*   **Risk:** Users acting outside of their intended permissions.
*   **Mitigation:** Holocron uses a robust 12-Factor Federated Authentication system.
    *   **Identity Broker:** An internal Identity Broker validates and issues JWTs based on externally trusted Identity Providers (Google, GitHub) or a local Mock provider.
    *   **Backend Verification:** Every authenticated endpoint in the backend via Armeria utilizes decorators to extract and validate the signature of the JWT using a secure `JWT_SECRET` environment variable. The decorator injects the resolved `UserId` into the gRPC Request Context.
    *   **Data Isolation:** The database layer (`CeremonyTemplateRepository` and others) enforces that queries are scoped to the `userId` in the context, preventing users from reading or modifying templates that do not belong to them.

## 2. Cryptographic Failures
*   **Risk:** Exposure of sensitive data due to weak or missing cryptography.
*   **Mitigation:**
    *   **In Transit:** Production deployments of Holocron should run behind an API Gateway/Load Balancer providing TLS (HTTPS). Armeria natively supports TLS configuration if deployed directly.
    *   **At Rest:** Passwords are not handled or stored by Holocron; it relies completely on external IdPs (OIDC/OAuth2). Session tokens (JWTs) are signed using HMAC-SHA256.

## 3. Injection
*   **Risk:** Untrusted data sent to an interpreter as part of a command or query.
*   **Mitigation:**
    *   **NoSQL Injection:** Holocron uses the official `mongodb-driver-kotlin-coroutine`. Queries are constructed using strongly-typed BSON builders (e.g., `Filters.eq("userId", userId)`), never via string concatenation.
    *   **Command Injection:** The application does not shell out or execute system commands based on user input.
    *   **Strong Typing:** The use of Protocol Buffers acting as the contract strictly defines the types and structure of data entering the system, fundamentally preventing arbitrary payload injection.

## 4. Insecure Design
*   **Risk:** Flaws in basic architecture or business logic.
*   **Mitigation:**
    *   **Contract-First Monorepo:** The architecture dictates a single source of truth (`ceremony.proto`). By generating typed clients and servers, a massive class of mismatch bugs and inconsistent validation logic is eliminated.
    *   **Threat Modeling:** The design intentionally isolates the Identity Broker from the core domain logic, and uses the "Indexed Metadata + Opaque Blob" pattern for templates to ensure data consistency without building complex relational models that are prone to access control misses.

## 5. Security Misconfiguration
*   **Risk:** Insecure default settings, open cloud storage, misconfigured HTTP headers.
*   **Mitigation:**
    *   The application relies entirely on environment variables for sensitive configuration (`AUTH_GOOGLE_CLIENT_ID`, `JWT_SECRET`, etc.), ensuring secrets don't leak into the repository.
    *   Angular's build process optimizes and strips out development-only artifacts.

## 6. Vulnerable and Outdated Components
*   **Risk:** Using libraries, frameworks, or software modules with known vulnerabilities.
*   **Mitigation:**
    *   Continuous usage of dependabot/renovate to keep Gradle (Armeria, Kotlin Coroutines, MongoDB driver) and NPM (Angular 19, Connect-RPC) dependencies up to date.
    *   The stack relies on major, heavily vetted frameworks (Armeria, Angular) rather than a patchwork of small micro-libraries.

## 7. Identification and Authentication Failures
*   **Risk:** Weak session management or credential handling.
*   **Mitigation:**
    *   Holocron does not manage passwords. It exclusively delegates authentication to major OIDC providers (Google, GitHub) or uses a strictly isolated mock setup for local dev.
    *   JWTs are stateless, verifiable, and configured with appropriate expiration times.

## 8. Software and Data Integrity Failures
*   **Risk:** Code or infrastructure that does not protect against integrity violations (e.g., pulling untrusted CI/CD plugins, insecure deserialization).
*   **Mitigation:**
    *   **Deserialization:** By using Protobuf to serialize and deserialize network payloads and database blobs, Holocron uses a safe, schema-bound binary format. It is immune to classic Java Object Serialization vulnerabilities.
    *   Gradle and NPM lockfiles (`package-lock.json`) are used to ensure deterministic and tamper-evident builds.

## 9. Security Logging and Monitoring Failures
*   **Risk:** Lack of visibility into attacks or failures.
*   **Mitigation:**
    *   Armeria's decorator system naturally integrates with SLF4J/Logback for comprehensive request logging. Future phases can expand this to emit structured JSON logs for centralized aggregators.
    *   gRPC Status codes naturally translate to appropriate HTTP status codes for monitoring dashboards to alert on anomaly rates.

## 10. Server-Side Request Forgery (SSRF)
*   **Risk:** The application fetching a remote resource without validating the user-supplied URL.
*   **Mitigation:**
    *   Holocron currently does not feature any functionality where a user can dictate an outbound HTTP request from the backend to an arbitrary URL. The only outbound requests are strictly defined configurations linking to trusted Identity Providers for token exchange.

## 11. Custom Audit & Dynamic Analysis (Added Post-Deployment)
As part of an active security review and fuzzing cycle:

### Exception Handling & Information Disclosure
*   **Finding:** The Kotlin backend contained instances where generic `Exception` types were caught and swallowed (printing only a basic `e.message`). This masked the underlying stack traces, making debugging difficult, and also inadvertently caught Kotlin coroutine `CancellationException`s, interfering with structured concurrency.
*   **Mitigation:** `Server.kt`, `MockAuthDecorator.kt`, and `S3StorageProvider.kt` were patched to properly re-throw `CancellationException`s and execute `e.printStackTrace()` to ensure errors are not silently masked.

### UI Fuzz Testing & Injection
*   **Methodology:** An automated browser agent was used to fuzz the Ceremony Creator and Responder UIs with excessively long strings, emojis, missing data, and common XSS payloads (`<script>alert(1)</script>`, etc.).
*   **Findings:**
    *   **XSS:** The Angular frontend strictly escaped all injected HTML payload strings. The backend correctly persisted and returned them without executing them. **Safe.**
    *   **Data Integrity:** The storage layer correctly persisted emojis, special characters, and fuzzed form schemas. Rapid UI interactions (drag and drop) did not corrupt data.
    *   **UI Stability (Patched):** The fuzz test discovered that excessively long strings with no spaces caused a horizontal overflow on the Dashboard ceremony cards, breaking the grid layout. This visual bug was **fixed** by adding text truncation and word-wrapping (`truncate` and `break-all` Tailwind utilities) to the template in `dashboard.component.ts`.

---
**Conclusion:** Holocron's foundational choices—Angular 19, Kotlin/Armeria, MongoDB, and Protobuf—provide a highly resilient baseline against the OWASP Top 10 vulnerabilities. The recent audit successfully closed application edge cases and hardened the platform further.
