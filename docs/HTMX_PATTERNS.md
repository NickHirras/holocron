# HTMX Implementation Patterns

## 🚀 Core Philosophy
We use HTMX to swap HTML fragments. Logic belongs in Java/Qute; the frontend is a thin "Holographic" layer.

## 🛠️ Common Patterns
1. **Trigger-Response**: Use `hx-post` or `hx-get` on semantic elements (buttons, inputs) to update specific segments of the "Cockpit" dashboard.
2. **Indicator/Loading**: Every interaction should have a `hx-indicator` that triggers a "Decrypting..." or progress bar animation.
3. **OOB (Out-of-Band) Swaps**: Use `hx-swap-oob="true"` to update global status bars (like the "HUD") while the user is inside a Ceremony flow.
4. **Validation**: Use `hx-validate="true"` for real-time input checks before the final "Transmission".

## 🚫 Constraints
- Do not use client-side state management (Redux, etc.).
- Avoid `<div>` soup; use semantic tags like `<article>` and `<nav>` to leverage **Pico CSS**.