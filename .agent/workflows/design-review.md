---
description: A multi-agent review process for new feature proposals to ensure Google-grade UX and technical excellence.
---

### 1. UX Foundation (Interaction Designer)
**Action**: Analyze the proposed feature's user flow.
**Focus**: 
- Is the navigation intuitive?
- Are all edge cases (loading, error, empty states) accounted for?
- Does it follow established mental models?

### 2. User Advocacy (UX Researcher)
**Action**: Audit the flow for friction and accessibility.
**Focus**:
- Identify "cognitive tax" (where the user has to think too hard).
- Check for accessibility roadblocks (WCAG compliance).
- Suggest one "Simplified" alternative to the proposed flow.

### 3. Technical Reality Check (Senior Software Engineer)
**Action**: Evaluate the implementation cost and scalability.
**Focus**:
- Will this impact P99 latency?
- Are there hidden complexities in the data model?
- What are the "unknown unknowns" or security risks?

### 4. Convergence (Consensus)
**Action**: Synthesize all feedback into a "Go/No-Go" summary.
**Output**: A list of "Required Changes" and "Strong Recommendations" formatted as a Markdown table.