package holocron.v1

import com.linecorp.armeria.common.HttpMethod
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.cors.CorsService
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.grpc.GrpcService
import io.grpc.protobuf.services.ProtoReflectionService
import com.mongodb.kotlin.client.coroutine.MongoClient
import holocron.v1.repository.CeremonyTemplateRepository
import java.util.UUID
import io.grpc.Status
import io.grpc.StatusException

class CeremonyServiceImpl(
    private val repository: CeremonyTemplateRepository
) : CeremonyServiceGrpcKt.CeremonyServiceCoroutineImplBase() {

    override suspend fun createCeremonyTemplate(request: CreateCeremonyTemplateRequest): CreateCeremonyTemplateResponse {
        val templateBuilder = request.template.toBuilder()
        
        if (templateBuilder.id.isEmpty()) {
            templateBuilder.id = UUID.randomUUID().toString()
        }
        
        val now = com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(System.currentTimeMillis() / 1000)
            .build()
            
        if (!templateBuilder.hasCreatedAt()) {
            templateBuilder.createdAt = now
        }
        templateBuilder.updatedAt = now
        
        val finalTemplate = templateBuilder.build()
        repository.save(finalTemplate)
        
        return CreateCeremonyTemplateResponse.newBuilder()
            .setTemplate(finalTemplate)
            .build()
    }

    override suspend fun getCeremonyTemplate(request: GetCeremonyTemplateRequest): GetCeremonyTemplateResponse {
        println("Received request for Template ID: ${request.templateId}")
        val template = repository.findById(request.templateId)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("Template not found"))
            
        return GetCeremonyTemplateResponse.newBuilder()
            .setTemplate(template)
            .build()
    }

    override suspend fun ping(request: PingRequest): PingResponse {
        println("Received ping: ${request.message}")
        return PingResponse.newBuilder().setMessage("Pong: ${request.message}").build()
    }
}

fun main() {
    val mongoClient = MongoClient.create("mongodb://localhost:27017")
    val repository = CeremonyTemplateRepository(mongoClient)

    // 1. Configure the gRPC Service
    val grpcService = GrpcService.builder()
        .addService(CeremonyServiceImpl(repository))
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

