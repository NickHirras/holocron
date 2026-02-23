# Project Holocron: Context & Roadmap

## Vision
A "Google Forms" style ceremony tool for engineering teams (Standups, Retros, Check-ins).
Built with the "Contract-First" (Protobuf) philosophy.

## Technical Stack
- **Source of Truth:** Protobuf (`/proto`)
- **Backend:** Kotlin (JVM) - Target: Android Studio / IntelliJ
- **Frontend:** Angular (TypeScript)
- **Communication:** Connect-RPC (over HTTP)
- **Tooling:** `buf` for linting and code generation, `make` for orchestration.

## Data Model Goals
Support complex item types similar to Google Forms:
- QuestionItem (Text, Choice, Scale)
- QuestionGroupItem
- Layout items (PageBreak, Image, Video)

## RBAC (Roles)
1. **Admin:** System-wide maintenance.
2. **Team Leader:** Manages templates, schedules, and team membership.
3. **Member:** Responds to ceremonies.

## The Golden Loop
1. Edit `.proto` in `/proto/holocron/v1/`.
2. Run `make gen`.
3. Implement logic in Kotlin/Angular using generated types.

## Current State
- Directory structure initialized.
- Schema definition in progress.

