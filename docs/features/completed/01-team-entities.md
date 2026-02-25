# Feature Spec: Team & Membership Entities

## Goal
Transition Project Holocron from a generic "personal form" tool to a team-centric "ritual" platform. This feature enables users to belong to specific teams (e.g., "Mobile Engineering", "Platform Team") and defines who has the authority to manage ceremonies versus who simply responds to them.

## Business Logic & Constraints
- **Team Ownership:** Every `CeremonyTemplate` must now be associated with a `TeamID`.
- **Membership Roles:** - `LEADER`: Can create/edit ceremonies and manage team members.
    - `MEMBER`: Can see team ceremonies and submit responses.
- **Shadow Profiles:** Continue to use the "Mock Header" pattern for local development, but when a user is fetched/created, they should be able to query their team affiliations.

## Step 1: Contract Changes (`proto/holocron/v1/ceremony.proto`)
1. **Define Team Message:**
   ```proto
   message Team {
     string id = 1;
     string display_name = 2;
     google.protobuf.Timestamp created_at = 3;
   }
   ```

2. **Define TeamMembership:**
   ```proto
   message TeamMembership {
     string team_id = 1;
     string user_id = 2;
     Role role = 3;

     enum Role {
       ROLE_UNSPECIFIED = 0;
       ROLE_MEMBER = 1;
       ROLE_LEADER = 2;
     }
   }
   ```

3. **Update User:** Add `repeated string team_ids = 5` to the `User` message.

4. **New Service Methods:** Add `CreateTeam`, `JoinTeam`, and `ListMyTeams` to the `UserService` or a new `TeamService`.

## Step 2: Backend Implementation (backend/src/main/kotlin)
1. **Repository:** Create a `TeamRepository` and `MembershipRepository` using the existing MongoDB coroutine pattern.

2. **Logic:** Update `CeremonyTemplateRepository` to strictly filter by `team_id`.

3. **Decorator:** Ensure the `MockAuthDecorator` or a new Interceptor can validate if a user belongs to the `team_id` they are trying to access.

## Step 3: Frontend Implementation (frontend/src/app)
1. **State:** Add a `TeamService` to track the "Current Active Team" using an Angular Signal.

2. **Dashboard Pivot:** Modify the `DashboardComponent` to show a "Team Switcher" dropdown. When a team is selected, the list of ceremonies should refresh to show only those belonging to the selected team.

3. **Guards:** Ensure users cannot navigate to a ceremony response page if they are not members of the owning team.

## Success Criteria
- Running `make gen` produces the new Team-related classes without errors.
- A user can create a "Team" and appear as its `LEADER`.
- A user can only see ceremonies that were created under their specific `team_id`.
