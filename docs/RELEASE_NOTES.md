# Release Notes: Holocron 1.0

We're thrilled to announce the 1.0 release of **Project Holocron**, a dynamic ceremony tool for engineering teams engineered with a strict Contract-First Monorepo Architecture.

## 🚀 Key Features and Capabilities

### The Ceremony Creator
Design standard agile ceremonies customized for your team's needs.
*   **Google Forms-style Builder:** An intuitive, drag-and-drop interface for structuring questions.
*   **Polymorphic Question Types:**
    *   Text Input (Short and Paragraphs)
    *   Multiple Choice & Checkboxes
    *   Grid Configurations
    *   Linear Scale (1-10 rankings)
    *   Date and Time selection
*   **Deep Customization:** Require answers, add helpful descriptions, and instantly preview how your team will view the payload.

### The Engaging Responder
Frictionless response logging for your team members.
*   Clean presentation layered on Angular 19 Standalone Signals.
*   Accessible markup guarantees a simple, fast interaction so engineers can get back to coding.
*   Robust validation rules configured natively from the schema.

### Real-time Insights (Dashboard)
Data is useless without a dashboard to visualize it.
*   **Cross-Tabulation:** Slice and compare responses to gain deeper team insights (e.g., comparing qualitative responses against quantitative "confidence" scores).
*   **Immediate Aggregation:** Responses are instantly tallied and presented via dynamic visual components that respond to the application's overall dark-theme seamlessly.

### Secure 12-Factor Authentication
A baked-in Identity Broker built for local and enterprise use cases.
*   **Mock Providers:** One-click authentication for local development, immediately signing test JWTs.
*   **Federated Logins:** Native support for mapping Google and GitHub OIDC flows securely into internal Application JWTs.
*   **Configurability via Environment Variables:** Use standard 12-Factor principles to manage provider configurations and dynamically redirect routes.

## 🛠️ Developer Experience Highlights

*   **Contract-Driven Development:** Adding fields to `ceremony.proto` instantly makes them strictly typed across the backend data layer and frontend client.
*   **Armeria Backbone:** One server efficiently routing declarative gRPC-Web and REST configurations natively on port 8080.
*   **MongoDB Coroutine Storage:** Reactive data persistence relying only on standard Kotlin features, minimizing latency and maximizing throughput.
*   **Clean Build Chains:** Utilizing modern tooling like `buf`, `gradle`, and native `npm` scripts under a single `Makefile` orchestration.

Enjoy using Holocron 1.0!
