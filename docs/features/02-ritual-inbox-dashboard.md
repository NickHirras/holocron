# Feature Spec: Ritual Inbox Dashboard

## Goal
Replace the current "Template-first" dashboard with an "Action-first" experience. Users should land on a page that highlights active ceremonies requiring their response (e.g., an open Sprint Retro or today's Daily Standup) while moving administrative tasks to the background.

## Business Logic & Constraints
- **Response Tracking:** The system must determine if a user has already submitted a response for a specific "instance" of a ceremony.
- **Recurrence Logic:** For the MVP, a ceremony is "Active" if it was created/updated within a specific window (e.g., the last 24 hours for Standups).
- **Role-Based Visibility:** - **Members:** See "Tasks for You" (Pending Responses) and "Team Pulse" (Recent Activity).
    - **Leaders:** See the above, plus "Manage Team Ceremonies" and "Facilitation Tools."

## Step 1: Contract Changes (`proto/holocron/v1/ceremony.proto`)
Add fields to assist in filtering and status tracking:
1. **Response Status Enum:**
   ```proto
   enum ResponseStatus {
     RESPONSE_STATUS_UNSPECIFIED = 0;
     RESPONSE_STATUS_PENDING = 1;
     RESPONSE_STATUS_COMPLETED = 2;
   }
   ```

2. **Dashboard Query:** Add `ListActiveCeremoniesRequest` to `CeremonyService` that returns templates along with the current user's `ResponseStatus`.

## Step 2: Backend Implementation (backend/src/main/kotlin)
1. **Service Logic:** Implement logic in `CeremonyServiceImpl` to join `CeremonyTemplate` data with `CeremonyResponse` data for the requesting user_id.

2. **Filtering:** Filter out "Public" templates that aren't relevant to the user's current team context unless explicitly searched.

## Step 3: Frontend Implementation (frontend/src/app)
1. **Dashboard Component Redesign:**

    - **Hero Section:** "Your Daily Rituals" – Large cards for pending standups/retros.
    - **Team Activity Feed:** A summary of how many people have checked in today.
    
2. **Navigation:** Move "Create New Ceremony" to a secondary "Admin" tab or a floating action button visible only to `LEADER` roles.

3. **State Management:** Use Angular Signals to reactively update the task list when a user submits a response.

## Success Criteria
- Upon login, a user sees a clear call-to-action for any unsubmitted ceremonies.
- Once a ceremony is submitted, it moves from the "Pending" section to a "Recent Activity" or "Completed" section.
- Users can toggle between multiple teams to see different ritual inboxes.

## Validating Your Work
Please run through the application using the web browser and validate all your changes are successful.

---

**Next Step:** Once you've added this to your `docs/features/` folder, I can provide the final spec for the **Facilitation (Live Meeting) Mode**, which handles how teams actually review the data they've collected during their meetings. Would you like that now?