# Ceremonies & Questions

## 1. Core Concept: Ceremony
A **Ceremony** is a structured interaction point for a team. It defines *when* questions are asked and *who* participates.

### Types of Ceremonies
*   **Async Standup**: Daily status update. Focus on "What did you do?", "What will you do?", "Blockers".
*   **Retrospective**: End-of-sprint reflection. Focus on "What went well?", "What didn't go well?", "Action Items".
*   **Team Pulse / Check-in**: Periodic (e.g., weekly) sentiment check. Focus on morale and workload.
*   **Planning Prep**: Pre-meeting data gathering for sprint planning.

---

## 2. Question Types
Questions are the building blocks of a Ceremony. Holocron supports various types to capture different kinds of data.

| Type | Description | UI Representation | Example |
| :--- | :--- | :--- | :--- |
| **Free Text** | Standard text input. Supports Markdown. | Textarea | "What did you work on yesterday?" |
| **Single Select** | Choose one option from a list. | Radio Buttons / Dropdown | "How are you feeling today?" (Great, Good, Okay, Bad) |
| **Multi Select** | Choose multiple options from a list. | Checkboxes | "Which projects did you touch?" (Proj A, Proj B, Proj C) |
| **Scale / Rating** | Numerical value within a range. | Slider / Star Rating | "Rate this sprint on a scale of 1-5." |
| **Boolean** | Yes / No. | Toggle Switch | "Do you have any blockers?" |
| **Integer** | Whole number input. | Number Input | "How many PRs are waiting for review?" |

### Question Configuration
*   **Required**: Must be answered to submit.
*   **Private**: Visible only to Team Leads (e.g., specific feedback).
*   **Pre-fill / Default**: Default value to show.
*   **Placeholder**: Helper text inside the input.

---

## 3. Smart Objects & Integrations
Beyond simple text, Holocron supports "Smart Objects" that enrich the data and integrate with external systems.

### 🛑 Blockers
*   **Definition**: Something preventing progress.
*   **Behavior**:
    *   Can be linked to an external issue (Jira, GitHub).
    *   Highlighted in the Team Dashboard.
    *   Persists until marked as "Resolved".

### 🏆 Kudos / Shout-outs
*   **Definition**: Public recognition of a team member.
*   **Behavior**:
    *   Tag a specific user (@User).
    *   Notification sent to the tagged user.
    *   Aggregated in "Leader Stats" or "Team Wins" summary.

### 📝 Action Items
*   **Definition**: A task generated from a Retrospective.
*   **Behavior**:
    *   Assignee + Due Date.
    *   **Rollover**: Uncompleted Action Items automatically appear in the *next* Retrospective for status updates.

### 🌡️ Sentiment / Mood
*   **Definition**: Tracking team morale over time.
*   **Behavior**:
    *   Visualized as a trend line in Reports.
    *   Anonymity options (Public, Team-Only, Leader-Only).

---

## 4. Example: "The Classic Standup"

**Frequency**: Daily, M-F, 9:00 AM local time.

1.  **Question 1 (Free Text)**: "What did you accomplish yesterday?"
    *   *Smart Integration*: Auto-suggest closed GitHub PRs / Jira tickets.
2.  **Question 2 (Free Text)**: "What are you working on today?"
3.  **Question 3 (Boolean + Text)**: "Do you have any blockers?"
    *   *Logic*: If "Yes", show text field for Blocker details.
