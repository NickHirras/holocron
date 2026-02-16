To be done in the future, here's the prompt to start it: 

```
Context: I am implementing the Universal Translator epic. We are moving from hardcoded strings to a localized message bundle system that supports two "Lingo Themes": "A Long Time Ago" (default Star Wars aesthetic) and "21st Century" (standard corporate/Scrum lingo).

Objective: Break down the implementation plan through the lenses of Engineering and UX to ensure a "Jedi-Grade" result.

Step 1: Technical Reality Check (Engineering)

Design a localized message bundle architecture using Quarkus i18n that allows for theme swapping via the HOLOCRON_LINGO_THEME environment variable.

Ensure the string resolution logic is O(1) and utilizes Caffeine caching to prevent latency impacts on the "Critical Path."

Draft the V1.0.5__Add_Lingo_Preference_Audit.sql migration to log theme initialization in the AuditEntry table.

Step 2: UX & Interaction Design (UX Engineer/Designer)

Audit base.html, dashboard.html, and pulse.html for hardcoded strings.

Define the "Lingo Mapping" for the "21st Century" theme to ensure it feels professional but isn't "div soup" or "mystery meat" navigation.

Validate that localized strings (especially in French or Spanish) won't break the Holographic Immersion typing animations or the 8dp linear scale grid.

Step 3: Convergence & Execution

Provide the Java TemplateExtensions required to expose these localized bundles to our Qute templates.

Generate a sample messages_en.properties for both lingo themes.

Constraints:

No client-side state management (Redux/Alpine.js).

Adhere strictly to the Google Java Style Guide.

Ensure all strings meet WCAG AA contrast ratios regardless of the lingo theme.

Transmission: Output a unified implementation roadmap and the starting code artifacts.
```
___

# Feature Task: Universal Translator (i18n & Lingo Themes)

> **Status**: Planned
> **Theme**: Infrastructure & Localization
> **Priority**: Medium (Quick Win)

## Objective
Implement a robust internationalization framework supporting standard languages (EN, ES, FR, PT-BR) and two distinct "Lingo Themes." The system allows organizations to toggle between the default Star Wars-inspired lingo and standard corporate terminology via a Docker environment variable.

## Tasks

### 1. Infrastructure & Schema
- [ ] **Configure i18n**: Set up `quarkus-qute` to support localized message bundles.
- [ ] **Environment Variable**: Add `HOLOCRON_LINGO_THEME` to `application.properties`, defaulting to `a_long_time_ago`.
- [ ] **Audit Logging**: Ensure lingo theme changes are captured in the `AuditEntry` table per **Data Integrity** protocols.

### 2. Message Bundle Architecture
- [ ] **"21st Century" Bundle**: 
    - Create `messages_en.properties` using standard corporate/Scrum terminology (e.g., "Daily Standup", "Submit Report").
- [ ] **"A Long Time Ago" Overlay**: 
    - Implement a mechanism (e.g., a custom `MessageBundle` producer) to prioritize "Jedi-Grade" lingo (e.g., "Command Deck", "Transmit Data") when configured.
- [ ] **Localization**: 
    - Provide `messages_es.properties` and `messages_fr.properties` translations for both themes.

### 3. UI Refactoring
- [ ] **Externalize Strings**: Replace all hardcoded strings in `base.html`, `dashboard.html`, and `pulse.html` with i18n keys (e.g., `{m:nav_home}`).
- [ ] **Holographic Immersion Compatibility**: Verify that localized strings of varying lengths do not break the "typing text" animation or layout.

### 4. Lingo Mapping Reference
| Key | 21st Century (Corporate) | A Long Time Ago (Jedi-Grade) |
| :--- | :--- | :--- |
| `nav_home` | Home / Dashboard | Command Deck |
| `pulse_start` | Start Standup | Initialize Protocol |
| `pulse_submit` | Submit Report | Transmit Data |
| `status_ok` | Submitted | Secure |
| `team_selector` | Switch Team | Sector Switcher |

## Acceptance Criteria
- [ ] The application defaults to the **"A Long Time Ago"** theme (Star Wars aesthetic).
- [ ] Setting `HOLOCRON_LINGO_THEME=21st_century` transforms the UI into standard corporate terminology.
- [ ] No hardcoded strings remain in Qute templates or Java controllers.

---
*"The Force will be with you. Always."*