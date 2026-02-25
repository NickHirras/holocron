# Current State & Next Steps

## What We Have Finished
- The Monorepo is scaffolded (Kotlin/Armeria, Angular 19).
- MongoDB Coroutine persistence and the Hexagonal Storage architecture are in place.
- Basic mock auth exists in the frontend.
- **12-Factor Federated Authentication** is implemented with an internal Identity Broker supporting Mock, Google, and GitHub logins via Armeria REST endpoints issuing JWTs.
  - Supports **OIDC Provider Overrides** via the `OIDC_ISSUER` environment variable for the Google Auth Provider, allowing local integration testing with mock IDPs.

## Immediate Next Goal: Final Review & Release (Phase 11)
- End-to-end audit of all features in the compiled production environment.
- OWASP Top 10 audit of the application.
- Update the README.md to contain overall information about Holocron. This is more of a marketing type landing page.  
- Move the "developers notes" to a separate document (linked from README.md) and ensure developer notes md is comprehensive, well-organized, and easy to navigate. It should give a new developer a good understanding of the project and how to contribute to it. They should know how to build, test, and deploy the project. They should have a good understanding of the project's architecture and design decisions. 
- Prepare formal release notes or a presentation summarizing the project's capabilities.