# 🌌 Project Holocron

Holocron is a ceremony tool built specifically for software engineering teams. It allows teams to define, distribute, and track agile ceremonies like Daily Standups, Sprint Retrospectives, and Incident Post-Mortems.

This project is built using a **Contract-First Monorepo Architecture**, heavily inspired by internal Google engineering standards.

## 🏗️ Architecture & Tech Stack

Holocron uses Protocol Buffers as the ultimate Source of Truth. The schema dictates the backend data classes, the frontend TypeScript interfaces, and the network transport layer.

* **Contract:** Protocol Buffers (`proto3`) managed by the `buf` CLI.
* **Backend:** Kotlin + Coroutines, powered by **Armeria**. Armeria natively serves gRPC, gRPC-Web, and REST on a single port without needing an Envoy proxy.
* **Database:** MongoDB. Uses the `mongodb-driver-kotlin-coroutine` and the "Indexed Metadata + Opaque Blob" architecture.
* **Frontend:** Angular 19 (Standalone Components, Signals) + **Connect-RPC** (speaking gRPC-Web over standard HTTP).
* **Build System:** Gradle (Backend), Vite/NPM (Frontend), orchestrated via a central `Makefile`.

---

## 🛠️ Prerequisites

Before you start, ensure you have the following installed on your machine:

1. **Java Development Kit (JDK):** Version 17 or higher (Required for Gradle/Kotlin).
2. **Node.js & NPM:** Node v22+ (Required for Angular).
3. **Buf CLI:** The modern Protobuf compiler.
   * `npm install -g @bufbuild/buf` (or via Homebrew: `brew install buf`)
4. **Make:** Standard GNU Make.
5. **grpcurl** (Optional but recommended): For testing gRPC APIs from the terminal.

---

## 🚀 Getting Started

### 1. Install Frontend Dependencies
The frontend relies on the Connect-RPC ecosystem. 
```bash
cd frontend
npm install
cd ..
```

### 2. Run the "Golden Loop" (Code Generation)
Before you can run the code, you must generate the Kotlin and TypeScript source files from the `.proto` definitions. 
```bash
make gen
```
*Note: This generates files into `backend/src/main/gen/` and `frontend/src/proto-gen/`. These directories are ignored by Git.*

### 3. Start the Database
Holocron depends on MongoDB for data persistence. Start the local database instance using Docker Compose:
```bash
docker compose up -d
```

### 4. Boot the Backend (Kotlin / Armeria)
The backend uses the Gradle wrapper to automatically download its dependencies and boot the Netty-based Armeria server.
```bash
make run-backend
```
* The gRPC-Web API will be live on `http://localhost:8080`.
* 🔍 **API Explorer:** Navigate to [http://localhost:8080/docs](http://localhost:8080/docs) to see the interactive Armeria UI.

### 5. Boot the Frontend (Angular)
In a new terminal tab, start the Angular development server:
```bash
cd frontend
npm start
```
* The web app will be live on [http://localhost:4200](http://localhost:4200).

### 6. Dynamic Mock Authentication (Local Dev)
For local development, Holocron uses a "Mock Header" authentication pattern (`x-mock-user-id`). The Angular application provides a dynamic login screen where you can input any email address (e.g., `creator@local` or `responder@local`). This email is stored in `localStorage` and automatically injected into all outbound gRPC-Web requests by an interceptor, making it trivial to test multi-user sharing and permissions locally without a real IdP.

---

## 🔄 The "Golden Loop" Workflow

We practice **Contract-Driven Development**. If you need to add a new feature (e.g., adding a "Confidence Score" to a Daily Standup), you never edit the Kotlin or TypeScript code first.

1. **Edit the Contract:** Open `proto/holocron/v1/ceremony.proto` and add your new message or field.
2. **Spin the Loop:** Run `make gen` from the root directory.
3. **Implement:** * Open your Kotlin IDE; the new field will instantly be available in the generated Data Classes.
   * Open your Web IDE; the new property will instantly be available with strict TypeScript typing.

---

## 📁 Monorepo Layout

```text
/holocron
├── Makefile                # Central orchestration commands
├── buf.work.yaml           # Buf workspace configuration
├── /proto                  # 📜 SOURCE OF TRUTH
│   ├── buf.yaml            # Linter & Breaking change rules
│   └── /holocron/v1        
│       └── ceremony.proto  # API & Domain Data Models
│
├── /backend                # ⚙️ KOTLIN G-RPC SERVER
│   ├── build.gradle.kts    # JVM Dependencies
│   └── /src/main
│       ├── /gen            # (Git Ignored) Auto-generated Java/Kotlin stubs
│       └── /kotlin         # Handwritten business logic (Server.kt)
│
└── /frontend               # 🎨 ANGULAR WEB APP
    ├── package.json        # Node Dependencies
    └── /src
        ├── /proto-gen      # (Git Ignored) Auto-generated TypeScript clients
        └── /app            # Handwritten Angular Components
```

---

## 🧪 Testing the API

You can test the backend API in three ways:

1. **Browser GUI:** Open `http://localhost:8080/docs` to use the built-in Armeria reflection UI.
2. **Angular UI:** Click the test button in the Angular app at `http://localhost:4200`.
3. **CLI:** Using `grpcurl` (Reflection is enabled on the server):
   ```bash
   grpcurl -plaintext \
     -d '{"template_id": "test-123"}' \
     localhost:8080 \
     holocron.v1.CeremonyService/GetCeremonyTemplate
   ```

