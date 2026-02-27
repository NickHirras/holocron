# 🌌 Project Holocron

**Holocron** is a dynamic, "Google Forms" style ceremony tool built specifically for software engineering teams. It empowers teams to seamlessly define, distribute, and track agile ceremonies like Daily Standups, Sprint Retrospectives, and Incident Post-Mortems.

## ✨ Key Features

- **Dynamic Ceremony Creator:** A flexible, intuitive interface allowing you to build ceremonies with multiple question types (Text, Multiple Choice, Grid, Linear Scale, Date/Time). Customize questions, specify requirements, and reorder them with drag-and-drop ease.
- **Action-Oriented Dashboard:** An inbox-style dashboard that prioritizes pending "Tasks for You" based on team assignments and distinguishes completed ceremonies into a "Team Pulse" view.
- **Engaging Responder Experience:** A clean, accessible presentation form for team members to fill out their ceremony updates quickly and efficiently.
- **Visual Insights Dashboard:** Instantly view aggregated results and cross-tabulate responses (e.g., comparing "Blockers" against "Confidence Levels").
- **Team Health Analytics:** Long-term insights aggregated over time to visualize trends in team sentiment, velocity, and blocker frequency.
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

---

## 🐳 Deployment & Containerization

Holocron is distributed as a single comprehensive Docker image containing the Angular frontend, Kotlin backend, and an internalized autostart MongoDB instance (for zero-configuration starts). 

### Single Instance (Zero-Config)
The easiest way to get started. Just map a volume to persist the internal MongoDB data, and map port `8080`.

```bash
docker run -d \
  -p 8080:8080 \
  -v holocron_data:/data/db \
  --name holocron \
  ghcr.io/nickhirras/holocron:latest
```

### Horizontally Scaled Multi-Instance
For enterprise deployments, you can spin up multiple instances of `holocron:latest` and configure them to bypass the internal DB/Cache by injecting environment variables pointing to external services.

```bash
docker run -d \
  -p 8080:8080 \
  -e "MONGODB_URI=mongodb://user:pass@external.mongo.host:27017/holocron?authSource=admin" \
  -e "CACHE_TYPE=distributed" \
  -e "REDIS_URI=redis://external.redis.host:6379" \
  -e "STORAGE_DRIVER=s3" \
  -e "S3_ENDPOINT=https://your-s3-endpoint.com" \
  -e "S3_BUCKET=holocron-assets" \
  # ... Add AWS credentials to your environment ...
  --name holocron \
  ghcr.io/nickhirras/holocron:latest
```

### 🧪 Local Test User Accounts
When running Holocron for the first time on a fresh database, a Database Seeder automatically runs to populate dummy history and user accounts. To test the mock Identity Broker in local development, use any of these emails in the login screen:
- `nick@nebula.io` (Admin / Original Template Creator)
- `maria@nebula.io` (Team Leader)
- `elena@nebula.io`
- `david@nebula.io`
- `sarah@nebula.io`
- `marcus@nebula.io`
- `jordan@nebula.io`
- `sam@nebula.io`
