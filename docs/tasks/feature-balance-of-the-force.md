# Feature Task: Balance of the Force (Light Mode Support)

> **Status**: Planned
> **Theme**: UI/UX & Accessibility
> **Priority**: Medium (Quick Win)

## Objective
Implement a high-fidelity Light Mode theme using **Pico CSS v2** variables and custom "Light Side" aesthetics. The goal is to provide a clean, high-contrast professional alternative to the current "Deep Space" theme while maintaining the "Jedi Archive" industrial feel.

## Tasks
- [ ] **Theme Variable Definition**
    - [ ] Define the "Jedi Temple" color palette in `holocron.css` inside a `@media (prefers-color-scheme: light)` block.
    - [ ] **Background**: High-contrast white/parchment (`#f8f9fa`) to replace *Void Black*.
    - [ ] **Primary**: "Guardian Blue" (`#007bff`) for action buttons, replacing *Holocron Cyan*.
    - [ ] **Accents**: "Master's Green" (`#28a745`) for success states.
- [ ] **Holographic Adaptation**
    - [ ] Adjust **Scanline Effect** opacity for light backgrounds (reduce to 1-2% from current 3-5%).
    - [ ] Swap "glow" box-shadows for subtle "depth" shadows to maintain the "Datapad" feel.
    - [ ] Update the **Holocron Cube** wireframe to use darker stroke values when in light mode.
- [ ] **Accessibility Audit**
    - [ ] Ensure all color pairings meet **WCAG AA contrast ratios (4.5:1)**.
    - [ ] Verify that **Faded Transmission** text (`#4b5b6e`) remains legible against the lighter surface.
- [ ] **Theme Toggle (The Sector Selector)**
    - [ ] Add a theme toggle switch to the **HUD** or **Profile** page.
    - [ ] Use **HTMX** to persist the user's theme preference to the `User` entity without a full page reload.

## Acceptance Criteria
- [ ] The site automatically respects the user's system preference for Light Mode.
- [ ] All "Jedi-Grade" UI elements (chamfered edges, Orbitron headers) remain intact and legible.
- [ ] The **Balance of the Force** transition is smooth and does not break the "Alive Hologram" interactive feel.

---
*"The Force will be with you. Always."*