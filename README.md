# 🌌 Project Holocron

**Holocron** is a dynamic, "Google Forms" style ceremony tool built specifically for software engineering teams. It empowers teams to seamlessly define, distribute, and track agile ceremonies like Daily Standups, Sprint Retrospectives, and Incident Post-Mortems.

## ✨ Key Features

- **Dynamic Ceremony Creator:** A flexible, intuitive interface allowing you to build ceremonies with multiple question types (Text, Multiple Choice, Grid, Linear Scale, Date/Time). Customize questions, specify requirements, and reorder them with drag-and-drop ease.
- **Action-Oriented Dashboard:** An inbox-style dashboard that prioritizes pending "Tasks for You" based on team assignments and distinguishes completed ceremonies into a "Team Pulse" view.
- **Engaging Responder Experience:** A clean, accessible presentation form for team members to fill out their ceremony updates quickly and efficiently.
- **Visual Insights Dashboard:** Instantly view aggregated results and cross-tabulate responses (e.g., comparing "Blockers" against "Confidence Levels").
- **12-Factor Federated Authentication:** Comprehensive security out-of-the-box. Includes a robust Identity Broker with native support for Mock (local development), Google, and GitHub logins, using JWT-based sessions.
- **Contract-First Monorepo Architecture:** Built for scale, consistency, and a delightful developer experience using Protobufs defining the boundary between a Kotlin/Armeria backend and an Angular 19 frontend.

---

## 📖 For Developers

Are you looking to build, run, or contribute to Holocron? 

We've designed the architecture to provide the best possible developer experience, heavily inspired by modern engineering standards. You'll find a Kotlin Coroutines backend, an Angular 19 Standalone Signals frontend, and an overarching Protobuf contract.

👉 **[Read the Full Developer Guide](docs/DEVELOPER_GUIDE.md)**

The Developer Guide includes:
- Architectural overviews and Tech Stack details.
- Prerequisites and Getting Started instructions.
- A guide to our **"Golden Loop"** Contract-Driven Development workflow.
- Details on testing the application locally, federated authentication configuration, and more.

---

## 🛠 Architecture Overview

Holocron uses Protocol Buffers (`proto3`) as its ultimate source of truth, dictating backend data models, frontend interfaces, and network transport.

- **Backend:** Kotlin + Coroutines over **Armeria**, serving gRPC, gRPC-Web, and REST natively on a single port.
- **Frontend:** Angular 19 using strictly Standalone Components and Signals, leveraging **Connect-RPC** to speak gRPC-Web natively.
- **Database:** MongoDB configured with coroutine drivers utilizing an "Indexed Metadata + Opaque Blob" storage pattern.
