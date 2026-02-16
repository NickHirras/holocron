# Feature Task: Balance of the Force (Light Mode Support)

> **Status**: Completed
> **Theme**: UI/UX & Accessibility
> **Priority**: Medium (Quick Win)

## Objective
Implement a high-fidelity Light Mode theme using **Pico CSS v2** variables and custom "Light Side" aesthetics. The goal is to provide a clean, high-contrast professional alternative to the current "Deep Space" theme while maintaining the "Jedi Archive" industrial feel.

## Tasks
- [x] **Theme Variable Definition**
    - [x] Define the "Jedi Temple" color palette in `holocron.css` inside a `@media (prefers-color-scheme: light)` block.
    - [x] **Background**: High-contrast white/parchment (`#f8f9fa`) to replace *Void Black*.
    - [x] **Primary**: "Guardian Blue" (`#007bff`) for action buttons, replacing *Holocron Cyan*.
    - [x] **Accents**: "Master's Green" (`#28a745`) for success states.
- [x] **Holographic Adaptation**
    - [x] Adjust **Scanline Effect** opacity for light backgrounds (reduce to 1-2% from current 3-5%).
    - [x] Swap "glow" box-shadows for subtle "depth" shadows to maintain the "Datapad" feel.
    - [x] Update the **Holocron Cube** wireframe to use darker stroke values when in light mode.
- [x] **Accessibility Audit**
    - [x] Ensure all color pairings meet **WCAG AA contrast ratios (4.5:1)**.
    - [x] Verify that **Faded Transmission** text (`#4b5b6e`) remains legible against the lighter surface.
- [x] **Theme Toggle (System Driven)**
    - [x] Implemented purely via `prefers-color-scheme` media query to reduce UI clutter.
    - [x] Removed manual toggle requirements in favor of automatic system detection.

## Acceptance Criteria
- [x] The site automatically respects the user's system preference for Light Mode.
- [x] All "Jedi-Grade" UI elements (chamfered edges, Orbitron headers) remain intact and legible.
- [x] The **Balance of the Force** transition is smooth and does not break the "Alive Hologram" interactive feel.

---
*"The Force will be with you. Always."*