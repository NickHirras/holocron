# Holocron HTMX Implementation Patterns

## 🚀 Core Philosophy
We prioritize server-side rendered HTML fragments over heavy JavaScript frameworks. The frontend is a thin "Holographic" layer that updates via HTMX swaps. We use HTMX to swap HTML fragments. Logic belongs in Java/Qute; the frontend is a thin "Holographic" layer.

## 🛠️ Common Patterns
1. **Trigger-Response**: Use `hx-post` or `hx-get` on semantic elements (buttons, inputs) to update specific segments of the "Cockpit" dashboard.
2. **Indicator/Loading**: Every interaction should have a `hx-indicator` that triggers a "Decrypting..." or progress bar animation.
3. **OOB (Out-of-Band) Swaps**: Use `hx-swap-oob="true"` to update global status bars (like the "HUD") while the user is inside a Ceremony flow.
4. **Validation**: Use `hx-validate="true"` for real-time input checks before the final "Transmission".

## 🛠️ Implementation Guardrails
1. **Targeted Swaps**: Use `hx-target` and `hx-swap` to update specific dashboard components (like the 'Council Rank' widget) without refreshing the entire page.
2. **Holographic Feedback**: Every POST/PUT request should utilize `hx-indicator` to trigger the "Decrypting..." or "Uploading..." animations defined in `UI_DESIGN.md`.
3. **Out-of-Band (OOB) Updates**: Use `hx-swap-oob="true"` to update the top-level HUD or Global Status bar while a user is deep within a ceremony flow.
4. **No Full-Page Reloads**: Avoid standard form submissions; use `hx-post` to maintain the "Alive Hologram" feel.

## 🚫 Constraints
- Do not use client-side state management (Redux, etc.).
- Avoid `<div>` soup; use semantic tags like `<article>` and `<nav>` to leverage **Pico CSS**.
- **Anti-Pattern**: Do not introduce client-side state management (e.g., Redux or complex Alpine.js stores).
- **Anti-Pattern**: Avoid `<div>` soup; stick to the semantic tags (`<article>`, `<nav>`) required by **Pico CSS**.
