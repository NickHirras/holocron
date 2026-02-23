# Current State & Next Steps

## What We Have Finished
- The Monorepo is fully scaffolded.
- The Protobuf schemas for the Form Builder are complete.
- The Armeria backend successfully receives gRPC-Web traffic natively.
- The Angular 19 frontend is connected via an InjectionToken and can ping the backend.

## Immediate Next Goal: The Database Layer
**Agent Task:** Implement the data persistence layer in the Kotlin backend to save and retrieve `CeremonyTemplate` objects.

### Architectural Constraints (The "Google Way")
1. **No ORMs:** Do not use Hibernate, JPA, or expose ORM concepts to the service layer.
2. **Database:** Use MongoDB. It naturally fits the deeply nested, polymorphic nature of our Protobuf documents.
3. **Driver:** Use the official `org.mongodb:mongodb-driver-kotlin-coroutine` to maintain non-blocking parity with Armeria.
4. **Data Pattern (Indexed Metadata + Opaque Blob):** - Create a Kotlin data class wrapper (e.g., `CeremonyTemplateDocument`) to represent the MongoDB document.
   - Extract highly queryable fields (like `id`, `authorId`, `createdAt`) as top-level indexed fields in the document.
   - Serialize the actual Protobuf `CeremonyTemplate` to a `ByteArray` and store it as a single opaque blob in the document. Do *not* attempt to map every individual question/choice into separate MongoDB fields.

### Execution Steps
1. Create a `docker-compose.yml` in the root of the project to spin up a local MongoDB instance.
2. Update `backend/build.gradle.kts` to include the MongoDB Coroutine driver.
3. Create a `CeremonyTemplateRepository.kt` class that implements the "Metadata + Blob" save and retrieve logic.
4. Update `CeremonyServiceImpl` in `Server.kt` to initialize the MongoDB connection, instantiate the repository, and use it inside the `createCeremonyTemplate` and `getCeremonyTemplate` RPC methods.
