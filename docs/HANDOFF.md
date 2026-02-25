# Project Holocron: Session Handoff

## Current State: Polish, Export, & Production Readiness (Phase 8)
We have successfully implemented Phase 8, tidying the UI and expanding data portability for the Holocron application.

1. **Data Export & CSV Enhancements 📈**:
   - Expanded CSV export capabilities in the `CeremonyResultsComponent`.
   - Users can now export specialized Cross-tabulated summaries directly into a pristine CSV containing the exact intersection grid shown in the UI.
   - Verified that frontend Date Filters seamlessly plug into both the existing and new CSV export functions.
2. **UI Polish & Animations ✨**:
   - Integrated native Angular View Transitions (`withViewTransitions`) for buttery smooth fade transitions between the Dashboard, Template Creator, and Results views.
   - Upgraded generic empty text placeholders with premium, centralized indicator cards containing interactive CTAs (e.g., "Create Your First Ceremony" and "No responses found").
   - Implemented a functional "Recent Activity" slide-out drawer on the dashboard, complete with a backdrop blur and a simulated chronological feed of system events.

## Current State: Scalability & Review (Phase 9) Completed
We have successfully implemented Phase 9, preparing Holocron for production deployment and improving maintainability.

1. **Angular Refactoring**: Extracted form-to-protobuf mapping logic from `CeremonyCreator` into `CeremonyMapperService`.
2. **RxJS Management**: Added `takeUntilDestroyed` with `DestroyRef` to dangling subscriptions in `CeremonyCreator` and `CeremonyResultsComponent` to prevent memory leaks during navigation.
3. **Production Dockerization**:
   - Built a multi-stage `backend/Dockerfile` using Gradle `installDist` and a lightweight JRE 21 execution environment.
   - Built a multi-stage `frontend/Dockerfile` wrapping the optimized standalone Angular build in Nginx (`nginx:alpine`) with SPA fallback routing.
   - Tied it together with a complete `docker-compose.prod.yml` ready for deployment alongside MongoDB.

## Phase 10: Image Uploads Completed
We implemented native drag-and-drop image uploading for the Ceremony Creator's `IMAGE` block.
- **Backend Storage**: Created `ImageRepository.kt` to store file chunks directly in MongoDB (capped at 10MB per image). Native Armeria `@Post` and `@Get` annotations are used on `ImageUploadService` to accept raw `application/octet-stream` byte payloads and serve them back blazingly fast without gRPC overhead.
- **Frontend Component**: Built a reusable, standalone `<app-image-uploader>` component using Angular Signals and strict native DOM drag-and-drop events (no `NgxDropzone` dependencies). It seamlessly uploads via `ImageUploadService` and patches the returned URL straight into the Reactive Form.

Holocron is currently feature-complete and containerized. The repository is technically ready to be tagged for a `v1.0.0` release.

# Objective for Next Session: 

I don't like storing images and other binary assets in MongoDB. It's not scalable and it's not efficient. I want to implement a file storage architecture that is agnostic to the storage backend. It should be able to swap between ephemeral memory, local disk, and S3-compatible cloud storage purely via environment variables, without changing business logic.  It should default to "In Memory" so when a developer just downloads and runs the project, they can test and explore it with zero up front configuration needed.

## Immediate Next Goal: Agnostic File Storage Architecture (12-Factor App)
**Agent Task:** Implement a "Port and Adapter" file storage architecture in the Kotlin backend to handle user uploads (like images). The system must be able to swap between ephemeral memory, local disk, and S3-compatible cloud storage purely via environment variables, without changing business logic.

### Architectural Constraints
1. **The Port (Interface):** Create a `FileStorageProvider` interface in Kotlin. It should have methods to save bytes and retrieve bytes, and it should deal exclusively in agnostic URIs (e.g., `holocron://assets/my-image.png`).
2. **The Adapters (Implementations):**
   - `InMemoryStorageProvider`: Uses a `ConcurrentHashMap` for ephemeral testing.
   - `LocalStorageProvider`: Uses standard Kotlin/Java NIO to save files to a configured local directory.
   - `S3StorageProvider`: Uses the official AWS SDK for Kotlin to stream bytes to an S3-compatible bucket (must support custom endpoints for MinIO/R2).
3. **The Factory:** Create a factory or provider object that reads the `STORAGE_DRIVER` environment variable at boot time (`memory`, `local`, or `s3`) and instantiates the correct adapter.
4. **Database Rule:** The application must NEVER store physical file paths (`/var/uploads/...`) or direct cloud URLs (`https://s3.amazonaws...`) in MongoDB. It must only store the agnostic `holocron://` URI.

### Execution Steps
1. **Dependencies:** Add `aws.sdk.kotlin:s3` to `backend/build.gradle.kts`. (Ensure it is compatible with Kotlin Coroutines).
2. **Core Interface:** Define `FileStorageProvider.kt`.
3. **Implement Adapters:** Write the three implementation classes. 
4. **Wiring:** Create a `StorageFactory` to read environment variables (e.g., `STORAGE_DRIVER`, `STORAGE_PATH`, `S3_BUCKET`, `S3_ENDPOINT`).
5. **Armeria Integration:** Update `Server.kt` to instantiate the active storage provider on boot. Add a simple REST endpoint (e.g., `POST /api/uploads`) in Armeria using the `DocService` or an annotated service to test the file upload and URI generation.