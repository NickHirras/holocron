# Feature: Beskar Security (Audit & Hardening)

## Strategic Value
The application currently runs in a "dev-only" mode with hardcoded users and no audit trail. This feature prepares Holocron for production by enforcing real authentication boundaries and logging all critical modifications, ensuring compliance and data integrity.

## User Story
As a **Compliance Officer**,
I want to see a log of who changed a team's schedule or configuration,
So that we can trace unauthorized changes.

As a **Developer**,
I want the application to use the logged-in user's identity instead of hardcoded strings,
So that I can test role-based access control (RBAC).

## Technical Blueprint
1.  **Audit Entity**: Create `AuditEntry` (id, who, what, when, old_value, new_value).
2.  **Migration**: Add Flyway script for `audit_entries` table.
3.  **Refactor PulseController**:
    *   Inject `SecurityContext`.
    *   Replace `User.findByEmail("alice@...")` with `User.findByIdentity(principal.getName())`.
4.  **Interceptors**: Implement an `@Audited` annotation and interceptor to automatically log changes to `Ceremony` or `Team` entities.

## Acceptance Criteria
- [ ] All `POST`/`PUT` requests to core controllers are logged to the `audit_entries` table.
- [ ] `PulseController` throws 401/403 if no valid user is logged in.
- [ ] Hardcoded "Alice" references are removed.
- [ ] `TeamMember` constraints are enforced (users can only see their own team's active pulses).
