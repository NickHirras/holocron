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
import holocron.v1.repository.UserRepository
import holocron.v1.repository.CeremonyResponseRepository
import holocron.v1.storage.FileStorageProvider
import holocron.v1.storage.StorageFactory
import holocron.v1.repository.TeamRepository
import kotlinx.coroutines.launch
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.common.MediaType
import kotlinx.coroutines.future.await

class CeremonyServiceImpl(
    private val templateRepository: CeremonyTemplateRepository,
    private val responseRepository: CeremonyResponseRepository,
    private val teamRepository: TeamRepository
) : CeremonyServiceGrpcKt.CeremonyServiceCoroutineImplBase() {

    override suspend fun createCeremonyTemplate(request: CreateCeremonyTemplateRequest): CreateCeremonyTemplateResponse {
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
            ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing user authentication"))

        val templateBuilder = request.template.toBuilder()
        val teamId = templateBuilder.teamId
        
        if (teamId.isEmpty()) {
            throw StatusException(Status.INVALID_ARGUMENT.withDescription("team_id is required"))
        }

        val membership = teamRepository.getMembership(teamId, userEmail!!)
        if (membership == null || membership.role != TeamMembership.Role.ROLE_LEADER) {
            throw StatusException(Status.PERMISSION_DENIED.withDescription("Only team leaders can create templates"))
        }
        
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
        templateBuilder.creatorId = userEmail // Set the creator to the authenticated user
        
        val finalTemplate = templateBuilder.build()
        templateRepository.save(finalTemplate)
        
        return CreateCeremonyTemplateResponse.newBuilder()
            .setTemplate(finalTemplate)
            .build()
    }

    override suspend fun getCeremonyTemplate(request: GetCeremonyTemplateRequest): GetCeremonyTemplateResponse {
        println("Received request for Template ID: ${request.templateId}")
        val template = templateRepository.findById(request.templateId)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("Template not found"))
            
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)

        if (!template.isPublic) {
            if (userEmail == null) {
                throw StatusException(Status.UNAUTHENTICATED.withDescription("Authentication required for private templates"))
            }
            val membership = teamRepository.getMembership(template.teamId, userEmail)
            if (membership == null && !template.sharedWithEmailsList.contains(userEmail)) {
                 throw StatusException(Status.PERMISSION_DENIED.withDescription("You are not a member of this team"))
            }
        }
            
        return GetCeremonyTemplateResponse.newBuilder()
            .setTemplate(template)
            .build()
    }

    override suspend fun listCeremonyTemplates(request: ListCeremonyTemplatesRequest): ListCeremonyTemplatesResponse {
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
            ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing user authentication"))

        val teamId = request.teamId
        if (teamId.isEmpty()) {
            throw StatusException(Status.INVALID_ARGUMENT.withDescription("team_id is required"))
        }

        val membership = teamRepository.getMembership(teamId, userEmail)
        if (membership == null) {
            throw StatusException(Status.PERMISSION_DENIED.withDescription("You are not a member of this team"))
        }

        // Fetch using DB query instead of findall
        val templates = templateRepository.findByTeamId(teamId).filter { 
            it.creatorId == userEmail || it.sharedWithEmailsList.contains(userEmail) || it.teamId == teamId
        }
        return ListCeremonyTemplatesResponse.newBuilder()
            .addAllTemplates(templates)
            .build()
    }

    override suspend fun listActiveCeremonies(request: ListActiveCeremoniesRequest): ListActiveCeremoniesResponse {
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
            ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing user authentication"))

        val teamId = request.teamId
        if (teamId.isEmpty()) {
            throw StatusException(Status.INVALID_ARGUMENT.withDescription("team_id is required"))
        }

        val membership = teamRepository.getMembership(teamId, userEmail)
        if (membership == null) {
            throw StatusException(Status.PERMISSION_DENIED.withDescription("You are not a member of this team"))
        }

        val templates = templateRepository.findByTeamId(teamId).filter { 
            it.creatorId == userEmail || it.sharedWithEmailsList.contains(userEmail) || it.teamId == teamId
        }

        val now = System.currentTimeMillis() / 1000
        val activeCeremonies = mutableListOf<ActiveCeremony>()

        for (template in templates) {
            val isStandup = template.title.contains("Standup", ignoreCase = true) || template.title.contains("Daily", ignoreCase = true)
            val activeWindowSecs = if (isStandup) 24 * 60 * 60 else 7 * 24 * 60 * 60

            val updatedOrCreatedAt = if (template.hasUpdatedAt()) template.updatedAt.seconds else template.createdAt.seconds
            val isActive = (now - updatedOrCreatedAt) <= activeWindowSecs

            if (isActive) {
                val hasResponded = responseRepository.hasResponded(userEmail, template.id)
                val status = if (hasResponded) ResponseStatus.RESPONSE_STATUS_COMPLETED else ResponseStatus.RESPONSE_STATUS_PENDING
                activeCeremonies.add(
                    ActiveCeremony.newBuilder()
                        .setTemplate(template)
                        .setResponseStatus(status)
                        .build()
                )
            }
        }

        return ListActiveCeremoniesResponse.newBuilder()
            .addAllActiveCeremonies(activeCeremonies)
            .build()
    }

    override suspend fun submitCeremonyResponse(request: SubmitCeremonyResponseRequest): SubmitCeremonyResponseResponse {
        val template = templateRepository.findById(request.response.ceremonyTemplateId)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("Template not found"))

        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)

        if (!template.isPublic) {
            if (userEmail == null) {
                throw StatusException(Status.UNAUTHENTICATED.withDescription("Authentication required for private templates"))
            }
            val membership = teamRepository.getMembership(template.teamId, userEmail)
            if (membership == null && !template.sharedWithEmailsList.contains(userEmail)) {
                 throw StatusException(Status.PERMISSION_DENIED.withDescription("You are not a member of this team"))
            }
        }

        val responseBuilder = request.response.toBuilder()
        
        if (responseBuilder.responseId.isEmpty()) {
            responseBuilder.responseId = UUID.randomUUID().toString()
        }
        
        val now = com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(System.currentTimeMillis() / 1000)
            .build()
            
        if (!responseBuilder.hasSubmittedAt()) {
            responseBuilder.submittedAt = now
        }
        
        responseBuilder.userId = userEmail ?: "anonymous"
        
        val finalResponse = responseBuilder.build()
        responseRepository.save(finalResponse)
        
        // Dispatch Notifications
        if (template.hasNotificationSettings()) {
            val settings = template.notificationSettings
            
            // 1. Email Notifications (Mock/Log)
            settings.emailAddressesList.forEach { email ->
                println("📧 [EMAIL NOTIFICATION] To: $email - Subject: New Response for ${template.title}")
            }
            
            // 2. Webhook Dispatch
            if (settings.webhookUrlsCount > 0) {
                val jsonPayload = """{"event": "response_submitted", "ceremony_template_id": "${template.id}", "user_id": "$userEmail"}"""
                @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                kotlinx.coroutines.GlobalScope.launch {
                    val client = com.linecorp.armeria.client.WebClient.of()
                    settings.webhookUrlsList.forEach { url ->
                        try {
                            println("🌐 Dispatching webhook to $url")
                            val req = com.linecorp.armeria.common.HttpRequest.builder()
                                .post(url)
                                .content(com.linecorp.armeria.common.MediaType.JSON_UTF_8, jsonPayload)
                                .build()
                            val response = client.execute(req)
                            response.aggregate().join()
                            println("✅ Webhook to $url dispatched successfully.")
                        } catch (e: Exception) {
                            if (e is kotlinx.coroutines.CancellationException) throw e
                            println("❌ Failed to dispatch webhook to $url: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        
        return SubmitCeremonyResponseResponse.newBuilder()
            .setResponse(finalResponse)
            .build()
    }

    override suspend fun listCeremonyResponses(request: ListCeremonyResponsesRequest): ListCeremonyResponsesResponse {
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
            ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing user authentication"))

        val template = templateRepository.findById(request.ceremonyTemplateId)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("Template not found"))
            
        if (template.creatorId != userEmail && !template.sharedWithEmailsList.contains(userEmail)) {
            throw StatusException(Status.PERMISSION_DENIED.withDescription("You do not have permission to view these responses"))
        }

        val responses = responseRepository.findByTemplateId(
            request.ceremonyTemplateId,
            if (request.hasFilterStartDate()) request.filterStartDate else null,
            if (request.hasFilterEndDate()) request.filterEndDate else null
        )
        return ListCeremonyResponsesResponse.newBuilder()
            .addAllResponses(responses)
            .build()
    }

    override suspend fun ping(request: PingRequest): PingResponse {
        println("Received ping: ${request.message}")
        return PingResponse.newBuilder().setMessage("Pong: ${request.message}").build()
    }
}

class ImageUploadService(private val storageProvider: FileStorageProvider) {
    @Post("/upload/image")
    suspend fun uploadImage(req: HttpRequest): HttpResponse {
        val contentType = req.contentType()?.toString() ?: "application/octet-stream"
        
        val aggregated = req.aggregate().await()
        val bytes = aggregated.content().array()
        
        if (bytes.isEmpty()) {
            return HttpResponse.of(HttpStatus.BAD_REQUEST, MediaType.PLAIN_TEXT_UTF_8, "Empty body")
        }
        
        if (bytes.size > 10 * 1024 * 1024) { // 10MB limit
            return HttpResponse.of(HttpStatus.REQUEST_ENTITY_TOO_LARGE, MediaType.PLAIN_TEXT_UTF_8, "File too large")
        }

        // Generate a random filename. We can parse extensions later if needed.
        val filename = UUID.randomUUID().toString()
        val uri = storageProvider.save(filename, bytes, contentType)
        
        return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, "{\"url\":\"$uri\"}")
    }

    @Get("/api/images/:id")
    suspend fun downloadImage(@Param("id") id: String): HttpResponse {
        val uri = "holocron://assets/$id"
        val fileData = storageProvider.get(uri)
        if (fileData == null) {
            return HttpResponse.of(HttpStatus.NOT_FOUND, MediaType.PLAIN_TEXT_UTF_8, "Image not found at $uri")
        }
        val (bytes, contentType) = fileData
        return HttpResponse.of(HttpStatus.OK, MediaType.parse(contentType), bytes)
    }
}

fun main() {
    val mongoClient = MongoClient.create("mongodb://localhost:27017")
    val templateRepository = CeremonyTemplateRepository(mongoClient)
    val responseRepository = CeremonyResponseRepository(mongoClient)
    val userRepository = UserRepository(mongoClient)
    val teamRepository = TeamRepository(mongoClient)
    
    // Initialize standard storage adapter (Memory by default)
    val storageProvider = StorageFactory.createActiveProvider()

    // 1. Configure the gRPC Service
    val grpcService = GrpcService.builder()
        .addService(CeremonyServiceImpl(templateRepository, responseRepository, teamRepository))
        .addService(UserServiceImpl(userRepository))
        .addService(TeamServiceImpl(teamRepository))
        .addService(ProtoReflectionService.newInstance())
        // Armeria enables gRPC-Web and REST fallback natively!
        .build()

    // 2. Build the all-in-one Server
    val server = Server.builder()
        .http(8080)
        // Global Auth Decorator
        .decorator(MockAuthDecorator())
        // Allow Angular (localhost:4200) to hit the server via browser preflight
        .decorator(
            CorsService.builderForAnyOrigin()
                .allowRequestMethods(HttpMethod.POST, HttpMethod.OPTIONS, HttpMethod.GET)
                .allowRequestHeaders(
                    "content-type", "x-grpc-web", "x-user-agent", "grpc-timeout", 
                    "x-mock-user-id", "X-Mock-User-Id", "connect-protocol-version",
                    "authorization", "Authorization"
                )
                .exposeHeaders("grpc-status", "grpc-message", "grpc-status-details-bin")
                .newDecorator()
        )
        // Mount gRPC
        .service(grpcService)
        // Mount REST Health Check
        .service("/healthz") { _, _ -> HttpResponse.of("Holocron Systems Operational.") }
        // Mount REST Auth Service
        .annotatedService(holocron.v1.auth.AuthRestService())
        // Mount REST Image Upload Service
        .annotatedService(ImageUploadService(storageProvider))
        // Mount the magical Web GUI
        .serviceUnder("/docs", DocService())
        .build()

    server.start().join()
    println("✅ Armeria Server running on port 8080")
    println("🔍 API Explorer available at: http://localhost:8080/docs")
}

