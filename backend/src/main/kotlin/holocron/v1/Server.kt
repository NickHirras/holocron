package holocron.v1

import com.linecorp.armeria.common.HttpMethod
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.cors.CorsService
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.grpc.GrpcService
import io.grpc.protobuf.services.ProtoReflectionService

class CeremonyServiceImpl : CeremonyServiceGrpcKt.CeremonyServiceCoroutineImplBase() {
    override suspend fun getCeremonyTemplate(request: GetCeremonyTemplateRequest): GetCeremonyTemplateResponse {
        println("Received request for Template ID: ${request.templateId}")
        return GetCeremonyTemplateResponse.getDefaultInstance()
    }

    override suspend fun ping(request: PingRequest): PingResponse {
        println("Received ping: ${request.message}")
        return PingResponse.newBuilder().setMessage("Pong: ${request.message}").build()
    }
}

fun main() {
    // 1. Configure the gRPC Service
    val grpcService = GrpcService.builder()
        .addService(CeremonyServiceImpl())
        .addService(ProtoReflectionService.newInstance())
        // Armeria enables gRPC-Web and REST fallback natively!
        .build()

    // 2. Build the all-in-one Server
    val server = Server.builder()
        .http(8080)
        // Allow Angular (localhost:4200) to hit the server via browser preflight
        .decorator(
            CorsService.builderForAnyOrigin()
                .allowRequestMethods(HttpMethod.POST, HttpMethod.OPTIONS)
                .allowRequestHeaders("content-type", "x-grpc-web", "x-user-agent", "grpc-timeout")
                .newDecorator()
        )
        // Mount gRPC
        .service(grpcService)
        // Mount REST Health Check
        .service("/healthz") { _, _ -> HttpResponse.of("Holocron Systems Operational.") }
        // Mount the magical Web GUI
        .serviceUnder("/docs", DocService())
        .build()

    server.start().join()
    println("✅ Armeria Server running on port 8080")
    println("🔍 API Explorer available at: http://localhost:8080/docs")
}

