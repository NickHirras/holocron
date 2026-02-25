# Current State & Next Steps

## What We Have Finished
- The Monorepo is scaffolded (Kotlin/Armeria, Angular 19).
- MongoDB Coroutine persistence and the Hexagonal Storage architecture are in place.
- Basic mock auth exists in the frontend.
- **12-Factor Federated Authentication** is implemented with an internal Identity Broker supporting Mock, Google, and GitHub logins via Armeria REST endpoints issuing JWTs.
  - Supports **OIDC Provider Overrides** via the `OIDC_ISSUER` environment variable for the Google Auth Provider, allowing local integration testing with mock IDPs.

## Immediate Next Goal: TBD
**Agent Task:** Await next instructions from the product owner.