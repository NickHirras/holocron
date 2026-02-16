---
description: A comprehensive three-stage process to audit the existing Holocron archives and ideate next-generation holographic features.
---

### 1. Codebase & Integrity Audit (Senior Software Engineer)
**Action**: Scan the current Java entities, controllers, and database migrations.
**Focus**:
- **Implementation Gaps**: Identify entities or services that are defined in `ARCHITECTURE.md` but lack full implementation (e.g., Artifact generation or specific Ceremony types).
- **Technical Debt**: Flag any hardcoded logic (like mock users in `PulseController.java`) that needs to be moved to a robust service.
- **Concurrency Check**: Audit new data paths for compliance with our **SQLite WAL** standards.

### 2. Friction Discovery (UX Researcher)
**Action**: Cross-reference the audit with our established `UX_FLOWS.md` and `UI_DESIGN.md`.
**Focus**:
- **Navigation Dead-ends**: Find features (like the Overseer Deck) that lack clear entry points in the current UI.
- **Persona Alignment**: Verify if the current interaction model matches the 'Alive Hologram' aesthetic and the Star Wars persona.

### 3. Galactic Expansion (Council Lead)
**Action**: Synthesize findings into a list of "Next-Gen" feature proposals.
**Output**: A Markdown table of 3-5 prioritized enhancements including:
- **Feature Name**: (e.g., "The Galactic Map" for team heatmaps).
- **Strategic Value**: Why it solves a real engineering problem.
- **Technical Blueprint**: High-level requirements (new entities, Qute fragments, or HTMX triggers).