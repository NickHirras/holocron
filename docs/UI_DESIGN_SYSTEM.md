# Holocron Design System: Space Console Implementation

This document serves as the technical specification for Antigravity agents to transform standard PicoCSS components into the "Star Wars Space Console" UI.

## 1. The Navigation (The "Dash")
Contrary to traditional SaaS sidebars, all primary navigation is housed in a **Bottom Console**.

- **Placement:** Fixed at the bottom of the viewport (`position: fixed; bottom: 0;`).
- **Structure:** Use a semantic `<nav>` within a footer or fixed container.
- **Visuals:** The console must have a "bezel" look—darker than the background with a top-edge "light leak" (cyan glow).

## 2. Global CSS Overrides
To maintain the "Used Future" aesthetic, use these variables and effects to override PicoCSS defaults in `holocron.css`.

### 2.1 The Holographic Scanline Overlay
Apply this to the main layout to simulate a CRT or holographic projection:
```css
.hologram-overlay {
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.1) 50%), 
              linear-gradient(90deg, rgba(255, 0, 0, 0.03), rgba(0, 255, 0, 0.01), rgba(0, 0, 255, 0.03));
  background-size: 100% 2px, 3px 100%;
  z-index: 100;
}
```

### 2.2 Component Directives
- Buttons as Physical Switches: Use box-shadow: inset 0 0 5px #000; to simulate recessed physical buttons. On hover, trigger a brightness(1.2) and a subtle flicker animation.
- Chamfered Edges: Use clip-path: polygon(10px 0, 100% 0, 100% calc(100% - 10px), calc(100% - 10px) 100%, 0 100%, 0 10px); for all buttons and article panels.
- The "Kyber" Status: Use a hexagonal status indicator in the HUD for active ceremony states.
  - Life-Support Green: Pulse when a ceremony is submitted.
  - Emergency Red: Flicker when a blocker is detected.

## 3. Typography & Lettering
- Data Display: Always use Share Tech Mono for interactive inputs and data tables.
- System Text: Use a small, dim font-size (0.7rem) with decorative strings (e.g., SYS-LOAD: 78%) near navigation items to simulate a running flight computer.

## 4. Interaction Logic
- Vertical Boot-up: On page load, content should scale from 0% to 100% height over 200ms using a vertical "wipe" effect to simulate a hologram powering on.
- No Mystery Meat: Icons must always be accompanied by labels or clear tooltips in monospaced font.

