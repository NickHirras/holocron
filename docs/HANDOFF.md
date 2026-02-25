# Project Holocron: Current State & Handoff

## Summary of Accomplishments

- **Phase 1: Team Entities (Completed)**
  - Updated `ceremony.proto` to include `Team` and `TeamMembership` models.
  - Implemented `TeamRepository` and `TeamServiceImpl` on the backend using Kotlin/MongoDB.
  - Enforced team-based access control inside `CeremonyServiceImpl`.
  - Added unit tests for backend services.
  - Developed an Angular `TeamService` on the frontend.
  - Built a dynamic "Team Switcher" dropdown in the Dashboard header.
  - Refactored frontend client logic to filter templates by `activeTeamId`.
  - Added Angular test coverage for the `TeamService`.
  - Verified compilation and passing tests across the stack.

## Next Features to Implement
(Please refer to `docs/features` for upcoming feature definitions).

## Developer Notes
- Ensure you run `./gradlew run` and `npm run start` in separate terminal windows.
- The `Team Switcher` automatically creates a "Shadow Profile" via the Auth Interceptor if one doesn't exist. Users start with an empty dashboard and are forced to create/join a team.
