# Design Review: Ceremony UX Redesign

> **Review Date**: 2026-02-18
> **Feature Plan**: [feature-ceremony-ux-redesign.md](../tasks/feature-ceremony-ux-redesign.md)
> **Status**: Approved with Requirements

## 1. UX Foundation (Interaction Designer)
**Analysis**:
The "Google Forms" model is a strong choice for this feature. The mental model of a "Canvas" for questions and a "Sidebar" for settings maps well to user expectations for builder tools.

**Edge Cases to Address**:
- **Sorting Failures**: If the server rejects a reorder (e.g., latency, auth), the UI must snap back to its original state to avoid data misalignment.
- **Save State Visibility**: "Auto-save on blur" is invisible. Users might not trust it. We need a clear visual indicator (e.g., "Saving..." -> "All changes saved") in the top toolbar or on the card itself.

## 2. User Advocacy (UX Researcher)
**Friction & Accessibility Audit**:
- **Critical Accessibility Gap**: Drag-and-drop via `SortableJS` is often inaccessible to keyboard users and screen readers.
- **Contrast**: The "Glassmorphism" aesthetic must not compromise input legibility. Ensure form fields have a background with at least 3:1 contrast against the container, and text has 4.5:1.

**Simplified Alternative**:
- Provide "Move Up" / "Move Down" arrow buttons on each card. This ensures accessibility and works reliably on touch devices where drag-and-drop can be finicky.

## 3. Technical Reality Check (Engineering)
**Implementation Cost & Risks**:
- **Latency**: `hx-trigger="change"` on text inputs could race if a user types fast and blurs quickly. Consider `hx-trigger="keyup changed delay:500ms"` for smoother updates, or explicit "Save" buttons if auto-save proves flaky.
- **Data Integrity**: Reordering a list involves updating multiple rows. Ensure the `reorder` endpoint is transactional.
- **Asset Management**: `SortableJS` needs to be served locally (not CDN) to allow for offline development and reduce external dependencies.

## 4. Convergence (Go/No-Go)

**Verdict**: **GO**, provided the following changes are made.

### Required Changes
| Category | Requirement |
| :--- | :--- |
| **Accessibility** | Implement **Up/Down buttons** for reordering as an alternative to Drag & Drop. |
| **UX** | Add a global **"Saving... / Saved"** status indicator that reacts to HTMX requests. |
| **Tech** | Wrap the `/reorder` endpoint logic in a `@Transactional` block. |
| **Tech** | Download `sortable.min.js` to `src/main/resources/META-INF/resources/assets/js/` (or equivalent) instead of using a CDN. |

### Strong Recommendations
- Use `hx-swap="outerHTML"` for question updates to prevent focus loss issues, or use out-of-band swaps for the status indicator.
- Add a "Duplicate" button for questions to speed up creation.
