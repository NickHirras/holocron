# Holocron: The Holographic Ceremony Engine 🌌

[![Powered by Google Gemini](https://img.shields.io/badge/Powered%20by-Google%20Gemini-8E75B2?style=for-the-badge&logo=googlegemini&logoColor=white)](https://deepmind.google/technologies/gemini/)
[![Built with Google Antigravity](https://img.shields.io/badge/Built%20with-Google%20Antigravity-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://deepmind.google/)

> *"Help us, Holocron. You're our only hope for better standups."*

Holocron is a premium, high-performance platform designed for elite engineering teams to manage their project ceremonies. Think of it as **Google Forms meets the Jedi Archives**—structured, automated, and beautifully low-friction.

[Screen recording 2026-02-20 9.28.54 AM.webm](https://github.com/user-attachments/assets/8a256acf-1cf9-4d13-acfe-c062ca5c58ad)

---

## ⚡ The Concept

Holocron eliminates "survey fatigue" by turning routine check-ins into **Ceremonies**. It leverages a modern, server-side rendered stack to deliver a "holographic" terminal-style experience that feels alive.

- **Ceremonies**: Structured interactions (Standups, Retros, Pulses).
- **Pulses**: Specific, scheduled occurrences of a ceremony.
- **Artifacts**: The immutable records generated from a completed pulse.

## 🛠️ Key Capabilities

- **[Async Standups](docs/CEREMONIES.md)**: Daily status updates that don't block your flow.
- **[Smart Integrations](docs/TECH_STACK.md#scheduling)**: Auto-suggest GitHub PRs and Jira tickets to reduce manual entry.
- **[Council Ranks](docs/GAMIFICATION.md)**: Engagement gamification with streaks and kudos.
- **[Multi-Channel Notifications](docs/NOTIFICATIONS.md)**: Engage via Slack, Teams, or Email.

## 🚀 The Tech Stack (Jedi-Grade)

Built by senior engineers for senior engineers, Holocron uses a "Supersonic Subatomic" foundation:

| Component | Technology | Why? |
| :--- | :--- | :--- |
| **Runtime** | **Java 25 LTS** | Virtual Threads for massive concurrency. |
| **Framework** | **Quarkus 3.x** | Optimized for containers and developer joy. |
| **Frontend** | **HTMX + Pico CSS** | Ultra-responsive, classless, and JavaScript-light. |
| **Database** | **SQLite + WAL** | Local, fast, and persistent. |
| **Cache** | **Caffeine** | Zero-infrastructure, low-latency performance. |

## 💻 Development

### Running in Dev Mode
To run the application in development mode with live reload:
```bash
./mvnw quarkus:dev
```
Access the application at [http://localhost:8080](http://localhost:8080).

### Quarkus Dev Tools
When running in dev mode, you have access to powerful tools:
*   **Dev UI**: [http://localhost:8080/q/dev](http://localhost:8080/q/dev) - Manage extensions, view configuration, and more.
*   **Swagger UI**: [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui) - Explore the API (if enabled).

### Building and Testing
*   **Compile**: `./mvnw clean package`
*   **Run Tests**: `./mvnw test`
*   **Integration Tests**: `./mvnw verify`

## 📖 Ship's Manifest (Documentation)

Navigate through the archives to learn more:

*   📜 **[Architecture](docs/ARCHITECTURE.md)**: RRule scheduling and data integrity.
*   🎭 **[Ceremonies & Questions](docs/CEREMONIES.md)**: Defining your team's rhythm.
*   🎮 **[Engagement Strategy](docs/GAMIFICATION.md)**: The "Council Rank" system.
*   🏗️ **[Technical Stack](docs/TECH_STACK.md)**: Deep dive into our choices.
*   🤖 **[Agent Guidelines](AGENT.md)**: How I (your AI assistant) operate.

---

## ⚖️ License
Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

---

**May the force be with you.**
