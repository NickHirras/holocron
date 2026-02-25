# Feature Spec: Facilitation (Live Meeting) Mode

## Goal
Transform the "Results" view into a specialized interface for live team meetings. This mode allows a Team Leader to facilitate discussions by controlling the visibility of responses, anonymizing data to encourage honesty, and highlighting key discussion points like blockers.

## Business Logic & Constraints
- **Role Gatekeeping:** Only users with the `LEADER` role for a team can toggle "Facilitation Mode" or "Reveal Answers".
- **Anonymity:** Support a "Safety First" toggle that masks the names of responders until the facilitator chooses to reveal them.
- **Real-time Sync:** While not requiring WebSockets for the MVP, the view should have a "Refresh" mechanism that doesn't disrupt the presentation flow.

## Step 1: Contract Changes (`proto/holocron/v1/ceremony.proto`)
Add fields to handle the presentation state:
1. **Facilitation Settings:**
   ```proto
   message FacilitationSettings {
     bool is_anonymized = 1;
     bool responses_visible = 2; // If false, hide answer content until "Reveal"
     string active_item_id = 3;  // The specific question the team is currently discussing
   }
   ```

2. **Update Template:** Add `FacilitationSettings facilitation_settings = 16` to the `CeremonyTemplate` message.

## Step 2: Backend Implementation (backend/src/main/kotlin)
1. **Authorization:** In CeremonyServiceImpl, ensure that identity fields in CeremonyResponse are stripped or masked if is_anonymized is true and the requester is not the Leader.

2. **State Persistence:** Allow the LEADER to update the FacilitationSettings via a partial update (PATCH) to the template.

## Step 3: Frontend Implementation (frontend/src/app)
1. **Component Update:** Modify CeremonyResultsComponent to include a "Facilitator Toolbar".

2. **Display Modes:**

    - Grid View: Standard summary of all answers.
    - Focus View: Shows one question at a time in large format for screen sharing.

3. **Identity Masking:** Use a UI pipe or conditional template to replace names with "Team Member" or "Anonymous" based on the `is_anonymized` signal.

## Success Criteria
A Team Leader can click "Enter Facilitation Mode" from any results page.

The UI can hide the identities of participants to facilitate unbiased retrospectives.

The "Focus View" allows the team to walk through questions one by one during a sync.

## Validating Your Work
Please run through the application using the web browser and validate all your changes are successful.


---

## How to use these with an Agent
Now that you have these three files in your `docs/features/` folder, you can prompt an AI agent as follows:

1.  **"Read GEMINI.md for our architecture and docs/features/01-team-entities.md for the first task."**
2.  **"Implement the Protobuf changes in Step 1 of the spec and run `make gen`."**
3.  **"Now move to Step 2 and implement the Backend repositories and services."**

Would you like me to help you update your `HANDOFF.md` to reflect that we've finished the "Ideation/Planning" phase and are ready for "Implementation"?