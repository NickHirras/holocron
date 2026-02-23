package holocron.v1

import io.grpc.ServerBuilder
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// This is where you will eventually put your database logic
class CeremonyServiceImpl : CeremonyServiceGrpcKt.CeremonyServiceCoroutineImplBase() {
    override suspend fun getCeremonyTemplate(request: GetCeremonyTemplateRequest): GetCeremonyTemplateResponse {
        println("Received request for Template ID: ${request.templateId}")
        return GetCeremonyTemplateResponse.getDefaultInstance()
    }
}

fun main() = runBlocking {
    // 1. Boot up the gRPC Server (Port 50051 is standard for gRPC)
    val grpcServer = ServerBuilder.forPort(50051)
        .addService(CeremonyServiceImpl())
        .build()
    
    grpcServer.start()
    println("✅ gRPC Server running on port 50051")

    // 2. Boot up Ktor (Port 8080 for HTTP traffic)
    launch(Dispatchers.IO) {
        embeddedServer(Netty, port = 8080) {
            routing {
                get("/healthz") {
                    call.respondText("Holocron Systems Operational.")
                }
            }
        }.start(wait = true)
    }

    // Keep the JVM alive
    grpcServer.awaitTermination()
}

