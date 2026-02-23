# Project Holocron: AI Agent Context

## Vision
Holocron is a "Google Forms" style dynamic ceremony tool for engineering teams (Standups, Retros).
It strictly follows a **Contract-First Monorepo** architecture.

## Technical Stack & Strict Constraints
- **Source of Truth:** Protobuf (`proto/holocron/v1/ceremony.proto`). *All data model changes must happen here first.*
- **Backend:** Kotlin (JVM) using **Armeria** (NOT Ktor, NOT Netty directly, NOT Spring Boot). Armeria handles gRPC-Web and REST natively on port `8080` without an Envoy proxy.
- **Frontend:** Angular 19 (Standalone Components, Signals). 
- **Networking:** `Connect-RPC` v2 (using `createGrpcWebTransport` and `createClient`).
- **Build Tools:** `buf` for proto compilation, Gradle for Kotlin, NPM for Angular. Orchestrated via `make`.

## The "Golden Loop" (Agent Instructions)
If you need to add a feature or change a data model, you MUST follow this exact sequence:
1. Modify `proto/holocron/v1/ceremony.proto`.
2. Run `make gen` from the project root.
3. NEVER manually edit files inside `backend/src/main/gen` or `frontend/src/proto-gen`. Treat these as read-only machine code.
4. Implement the logic in Kotlin (`backend/src/main/kotlin`) or TypeScript (`frontend/src/app`).

## Known Nuances (Do Not Attempt to Fix)
- The Kotlin backend uses `javax.annotation` and `jakarta.annotation` via Tomcat APIs to bypass a known JPMS Kotlin compiler bug. Do not change the Gradle dependencies for these annotations.

