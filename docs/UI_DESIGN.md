# Holocron UI/UX Design Concepts

## 🌌 Core Aesthetic: "Alive Hologram"
The interface should feel like a datapad or a holographic projection from the Star Wars universe. It's not just "dark mode"; it's **deep space** with **luminescent data**.

### 1. Color Palette
We avoid flat colors in favor of glowing, vibrant hues against a void-like background.

| Usage | Color Name | Hex Code | Effect |
| :--- | :--- | :--- | :--- |
| **Background** | *Void Black* | `#050a14` | The depths of space. Slightly navy, not pure black. |
| **Panel BG** | *Durasteel* | `#0d1b2a` | Semi-transparent (90%) with a subtle grid overlap. |
| **Primary** | *Holocron Cyan* | `#00e5ff` | Main action buttons, active states, key data. **Glows.** |
| **Secondary** | *Rebel Orange* | `#ff9d00` | Highlights, secondary actions. |
| **Success** | *Forest Moon* | `#00ff41` | Completed tasks, positive trends. |
| **Alert** | *Sith Red* | `#ff003c` | Errors, blockers, critical alerts. |
| **Text** | *Starlight* | `#e0fbfc` | Primary text. Slightly blue-tinted white. |
| **Dimmed** | *Faded Transmission* | `#4b5b6e` | Secondary text, placeholders. |

### 2. Typography
Typography must convey "high-tech industrial" but remain readable.

*   **Headers**: `Orbitron` (Google Fonts) - Geometric, futuristic, strong.
    *   *Usage*: Page titles, card headers, big stats.
    *   *Style*: Uppercase, wide tracking/letter-spacing.
*   **Body & Data**: `Share Tech Mono` or `Roboto Mono` - Monospaced, terminal-like.
    *   *Usage*: All inputs, descriptions, list items.
    *   *Style*: Crisp, legible size (14px+).

### 3. Visual FX & Textures
*   **Scanlines**: A subtle, scrolling horizontal line overlay (very low opacity, 3-5%) to give that CRT/Hologram feel.
*   **Glow/Bloom**: Primary elements (buttons, active borders) should have a `box-shadow` that simulates light emission.
*   **Glitch**: On page load or state change, elements "flicker" into existence (layout shift or opacity stutter) for 100ms.
*   **Grid**: Backgrounds should have a faint vector grid (major/minor lines) that fades at the edges.

### 4. Layout Principles: "The Datapad"
*   **Chamfered Edges**: No rounded corners (border-radius: 0). Instead, use `clip-path` to cut the corners at 45 degrees.
*   **Borders**: Thin, 1px borders. Active panels get a "shimmering" border or a brighter color.
*   **Modular**: Everything is a "module" or "card". The Dashboard is a cockpit of these modules.

---

## 🖥️ Core Page Concepts

### 1. Landing Page ("The Transmission")
*   **Hero**: A massive, rotating wireframe Holocron cube in pure CSS/SVG.
*   **Headline**: "TRANSMISSION RECEIVED: CLARIFY YOUR SIGNAL."
*   **CTA**: "Initialize Holocron" (Login/Signup) - A button that looks like a launch key.
*   **Vibe**: Mysterious, invitation-only feel.

### 2. User Dashboard ("The Cockpit")
*   **Layout**: Bento-grid style (variable sized cards).
*   **Modules**:
    *   **Standup Status**: Large indicator. "PENDING" (Blinking Amber) or "SUBMITTED" (Solid Green).
    *   **Team Pulse**: A sparkline graph showing team sentiment.
    *   **Galactic Map (Calendar)**: A grid showing past and upcoming ceremonies.
    *   **Comm Channels**: Recent notifications stream.

### 3. The Ceremony Flow ("The Interrogation")
*   **Focus Mode**: When answering questions, everything else fades out.
*   **Input**:
    *   Text areas look like terminal inputs (`> _`).
    *   Users "transmit" answers rather than "submit" forms.
    *   Sound effects (optional toggle): Subtle beeps on keypress or success.

---

## 🔄 Interaction Design
*   **Hover**: Elements brighten immediately (no slow fade).
*   **Active**: Elements slightly depress or shift pixel-down.
*   **Loading**: Not a spinner. A "Decrypting..." text animation or a progress bar that fills like a data upload.
