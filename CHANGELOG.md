# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Data Model**: Implemented `Ceremony` and `CeremonyQuestion` entities with repositories.
- **Dev Mode Login**: Implemented a "Star Wars" themed login/logout flow for development usage.
- **Landing Page**: Finalized implementation for production deployment.
- **Documentation**: Added "Google Gemini" and "Google Antigravity" badges to `README.md`.
- **DevOps**: Configured GitHub `CODEOWNERS` file, setting `NickHirras` as the code owner.
- **Local Development**: Set up local authentication mocking for Slack, Google Chat, and GitHub to facilitate offline testing.
- **Teams Management**: Implemented full CRUD functionality for managing teams and their members.
- **Reports & Stats**:
    - implemented "Team Daily Rollup" report.
    - Implemented "Member Historical View".
    - Implemented "Leader Stats" widget for the dashboard.
- **Authentication**: Implemented user registration, login, and Role-Based Access Control (RBAC).
- **Build**: Configured GraalVM build for a Java web application.

### Changed
- **Database**: Restored SQLite support for local development by resolving compatibility issues between Quarkus 3.15, Hibernate ORM, and `quarkus-jdbc-sqlite`.
- **Documentation**: Refined `CEREMONY_AND_QUESTIONS.md` to better outline ceremony structures.
- **Documentation**: Improved `AGENT.md` for better clarity and organization.
