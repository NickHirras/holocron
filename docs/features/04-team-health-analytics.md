# Feature Spec: Team Health & Analytics

## Goal
Provide Team Leaders and Leadership with long-term insights into team health and ceremony participation. Instead of looking at individual responses, this feature aggregates data over time to visualize trends in sentiment, velocity, and blocker frequency.

## Business Logic & Constraints
- **Aggregation Logic:** Data should be aggregated weekly or per-sprint.
- **Privacy vs. Insights:** While individual responses can be anonymous (refer to 03-facilitation-mode.md), the aggregate data is always visible to Leaders to identify trends.
- **Key Metrics:**
    - **Participation Rate:** Percentage of team members completing rituals.
    - **Sentiment Trend:** Average "Linear Scale" score from health-check questions.
    - **Blocker Count:** Frequency of items marked as "Blockers."

## Step 1: Contract Changes (`proto/holocron/v1/ceremony.proto`)
Add a new service and messages for analytics:
1. **Analytics Messages:**
   ```proto
   message TeamMetric {
     string metric_name = 1;
     float value = 2;
     google.protobuf.Timestamp timestamp = 3;
   }

   message GetTeamHealthRequest {
     string team_id = 1;
     google.protobuf.Timestamp start_time = 2;
     google.protobuf.Timestamp end_time = 3;
   }

   message GetTeamHealthResponse {
     repeated TeamMetric metrics = 1;
   }
   ```

2. **Service:** Add `GetTeamHealth` to a new `AnalyticsService` or the existing `CeremonyService`.

## Step 2: Backend Implementation (backend/src/main/kotlin)
1. **Aggregation Service:** Create an AnalyticsProvider that queries CeremonyResponseRepository.

2. **Logic:** Implement logic to calculate averages for LINEAR_SCALE question types and counts for specific keywords or "Blocker" flags.

3. **Efficiency:** For the MVP, calculate these on-the-fly. For future scale, consider a pre-aggregated "Metrics" collection in MongoDB.

## Step 3: Frontend Implementation (frontend/src/app)
1. **New View:** Create a TeamAnalyticsComponent.

2. **Visualizations:**

    - Use a simple Sparkline or Bar Chart (via Tailwind or a lightweight Chart library) to show sentiment over the last 5 rituals.

    - **"Hot Spots":** List the top 3 recurring blockers mentioned in the last month.

3. **Leadership View:** Allow a "System Admin" to see an org-wide heat map of which teams are "Green" (high engagement) vs "Red" (low engagement/low sentiment).

## Success Criteria
- A Team Leader can view a "Trends" tab on their dashboard.
- The system correctly calculates the average sentiment score from multiple standups.
- Leadership can identify if a team's health is declining over a 4-week period.


### Summary of the New Feature Set
With these 4 files, you have a complete roadmap for an agent to follow:
1.  **01-team-entities.md**: The foundational "Who" (Teams/Roles).
2.  **02-ritual-inbox-dashboard.md**: The daily "How" (The User Flow).
3.  **03-facilitation-mode.md**: The meeting "Value" (Collaborative Review).
4.  **04-team-health-analytics.md**: The long-term "Why" (Leadership Insights).

**Final TPM Tip:** When you start prompting the agent, tell it to focus on **01-team-entities** first. Do not let it try to build all four at once, or the code changes will become too large and prone to errors. Finish the "Golden Loop" for each file before moving to the next.