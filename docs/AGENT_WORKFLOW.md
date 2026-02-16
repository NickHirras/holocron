# Agent Workflow

## 🚨 CRITICAL PROTOCOL 🚨

This document serves as the **Prime Directive** for all AI Agents operating within this repository.

### 1. Folow the 4 C's:  
- **Condition**: The symptom or issue the customer is experiencing.
- **Cause**: The root cause of the problem.
- **Correction**: The repair or service performed.
- **Confirm**: Verifying the fix/confirming the condition is resolved.

**Condition** is what the user reports.
**Cause** is what you determine to be the root cause.
**Correction** is what you do to fix the issue.
**Confirm** is verifying the fix/confirming the condition is resolved.

**ALWAYS** test your changes. Don't assume you got it right. Run the tests, confirm the application starts up correctly, and verify the fix. Drive the browser and confirm everything is operating as expected. 

It's annoying when you say "it works for me" and I can't even start the application.  Please do better, you are a master of your craft. 

### 2. Branching Strategy
*   **NEVER** commit directly to `main`.
*   **ALWAYS** create a feature branch for your work.
    *   **Feature:** `feature/<short-description>`
    *   **Bugfix:** `fix/<issue-id>-<short-description>`
    *   **Chore:** `chore/<short-description>`

### 3. Pull Requests (PRs)
*   All changes must be submitted via a **Pull Request**.
*   **Target Branch:** `main`
*   **Assignee:** Assign the PR to the Code Owner: **`@NickHirras`**.
*   **Review:** Do not merge your own PR. Wait for Code Owner approval.

### 4. Commit Standards
*   Write clear, concise commit messages.
*   Reference issue numbers if applicable.

> "Do or do not. There is no try." - Adhere to this workflow strictly.
