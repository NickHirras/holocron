---
description: A two-stage process to brainstorm and vet the next era of Holocron features.
---

### 1. Vision Casting (Council Lead)
**Action**: Based on the current `CHANGELOG.md` and `TODO.md`, identify the next three logical "Epics."
**Prompt**: "What are the three biggest 'Force Multipliers' we can build to make Holocron indispensable for a remote engineering team?"

### 2. Technical Blueprinting (Senior Software Engineer)
**Action**: Review the "Epics" against the current `ARCHITECTURE.md` and `TECH_STACK.md`.
**Focus**: 
- Which ideas require new schema migrations?
- Which ideas can be implemented purely as new Qute templates or HTMX triggers?
- Identify potential performance bottlenecks in SQLite.

### 3. The "Council Summary"
**Output**: A prioritized list of features for the `TODO.md`, including:
- **Feature Name & Description**
- **User Value** (The "Why")
- **Implementation T-Shirt Size** (S, M, L)