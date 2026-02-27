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
import io.viascom.nanoid.NanoId
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
import holocron.v1.util.DatabaseSeeder
import kotlinx.coroutines.runBlocking
import holocron.v1.cache.CacheFactory
import holocron.v1.cache.CachePort

import java.time.Duration

class CeremonyServiceImpl(
    private val templateRepository: CeremonyTemplateRepository,
    private val responseRepository: CeremonyResponseRepository,
    private val teamRepository: TeamRepository,
    private val templateCache: CachePort<String, CeremonyTemplate>
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
            templateBuilder.id = NanoId.generate(12, "23456789abcdefghjkmnpqrstuvwxyz")
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
        templateCache.put(finalTemplate.id, finalTemplate, Duration.ofMinutes(30))
        
        if (finalTemplate.hasNotificationSettings() && finalTemplate.notificationSettings.webhookUrlsCount > 0) {
            val team = teamRepository.getTeams(listOf(finalTemplate.teamId)).firstOrNull()
            val teamName = team?.displayName ?: "Your Team"
            
            val frontendUrl = ctx.attr(MockAuthDecorator.FRONTEND_URL_ATTR) ?: "http://localhost:4200"
            
            holocron.v1.integration.WebhookDispatcher.dispatch(
                finalTemplate.notificationSettings.webhookUrlsList,
                holocron.v1.integration.WebhookDispatcher.EventType.CEREMONY_STARTED,
                holocron.v1.integration.WebhookDispatcher.WebhookContext(
                    templateId = finalTemplate.id,
                    templateTitle = finalTemplate.title,
                    teamName = teamName,
                    frontendUrl = frontendUrl
                )
            )
        }
        
        if (finalTemplate.hasNotificationSettings() && finalTemplate.notificationSettings.emailAddressesCount > 0) {
            val team = teamRepository.getTeams(listOf(finalTemplate.teamId)).firstOrNull()
            val teamName = team?.displayName ?: "Your Team"
            
            val frontendUrl = ctx.attr(MockAuthDecorator.FRONTEND_URL_ATTR) ?: "http://localhost:4200"
            
            holocron.v1.integration.EmailDispatcher.dispatch(
                finalTemplate.notificationSettings.emailAddressesList,
                "Ceremony Ready: ${finalTemplate.title}",
                "The ceremony ${finalTemplate.title} for team $teamName is now ready for your input!\nRespond here: $frontendUrl/ceremony/${finalTemplate.id}"
            )
        }
        
        return CreateCeremonyTemplateResponse.newBuilder()
            .setTemplate(finalTemplate)
            .build()
    }

    override suspend fun updateCeremonyTemplate(request: UpdateCeremonyTemplateRequest): UpdateCeremonyTemplateResponse {
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
            ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing user authentication"))

        val templateId = request.template.id
        if (templateId.isEmpty()) {
            throw StatusException(Status.INVALID_ARGUMENT.withDescription("template id is required"))
        }

        val existingTemplate = templateRepository.findById(templateId)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("Template not found"))

        val membership = teamRepository.getMembership(existingTemplate.teamId, userEmail)
        if (membership == null || membership.role != TeamMembership.Role.ROLE_LEADER) {
            throw StatusException(Status.PERMISSION_DENIED.withDescription("Only team leaders can update templates"))
        }

        val templateBuilder = request.template.toBuilder()
            .setTeamId(existingTemplate.teamId) // Keep original team
            .setCreatorId(existingTemplate.creatorId) // Keep original creator
            .setCreatedAt(existingTemplate.createdAt) // Keep original creation time

        val now = com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(System.currentTimeMillis() / 1000)
            .build()
        templateBuilder.updatedAt = now

        val finalTemplate = templateBuilder.build()
        templateRepository.save(finalTemplate)
        templateCache.put(finalTemplate.id, finalTemplate, Duration.ofMinutes(30))

        return UpdateCeremonyTemplateResponse.newBuilder()
            .setTemplate(finalTemplate)
            .build()
    }

    override suspend fun getCeremonyTemplate(request: GetCeremonyTemplateRequest): GetCeremonyTemplateResponse {
        var template = templateCache.get(request.templateId)
        if (template == null) {
            template = templateRepository.findById(request.templateId)
                ?: throw StatusException(Status.NOT_FOUND.withDescription("Template not found"))
            templateCache.put(template.id, template, Duration.ofMinutes(30))
        }
            
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
        templates.forEach { templateCache.put(it.id, it, Duration.ofMinutes(30)) }
        
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
        templates.forEach { templateCache.put(it.id, it, Duration.ofMinutes(30)) }

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
        var template = templateCache.get(request.response.ceremonyTemplateId)
        if (template == null) {
            template = templateRepository.findById(request.response.ceremonyTemplateId)
                ?: throw StatusException(Status.NOT_FOUND.withDescription("Template not found"))
            templateCache.put(template.id, template, Duration.ofMinutes(30))
        }

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
            responseBuilder.responseId = NanoId.generate(12, "23456789abcdefghjkmnpqrstuvwxyz")
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
            if (settings.emailAddressesCount > 0) {
                val frontendUrl = ctx.attr(MockAuthDecorator.FRONTEND_URL_ATTR) ?: "http://localhost:4200"
                holocron.v1.integration.EmailDispatcher.dispatch(
                    settings.emailAddressesList,
                    "New Response for ${template.title}",
                    "A new response was submitted for ${template.title} by ${userEmail ?: "anonymous"}.\nView results: $frontendUrl/dashboard"
                )
            }
            
            // 2. Webhook Dispatch
            if (settings.webhookUrlsCount > 0) {
                // Determine completion status
                val memberships = teamRepository.getTeamMemberships(template.teamId)
                val totalMembers = memberships.size
                
                // Get total responses so far
                val allResponses = responseRepository.findByTemplateId(template.id, null, null)
                val totalResponses = allResponses.size
                
                val team = teamRepository.getTeams(listOf(template.teamId)).firstOrNull()
                val teamName = team?.displayName ?: "Your Team"
                val frontendUrl = ctx.attr(MockAuthDecorator.FRONTEND_URL_ATTR) ?: "http://localhost:4200"

                // Send individual response notification
                holocron.v1.integration.WebhookDispatcher.dispatch(
                    settings.webhookUrlsList,
                    holocron.v1.integration.WebhookDispatcher.EventType.RESPONSE_SUBMITTED,
                    holocron.v1.integration.WebhookDispatcher.WebhookContext(
                        templateId = template.id,
                        templateTitle = template.title,
                        teamName = teamName,
                        userEmail = userEmail,
                        totalResponses = totalResponses,
                        totalMembers = totalMembers,
                        frontendUrl = frontendUrl
                    )
                )
                
                // If all members have responded, send completion notification
                if (totalMembers > 0 && totalResponses >= totalMembers) {
                    holocron.v1.integration.WebhookDispatcher.dispatch(
                        settings.webhookUrlsList,
                        holocron.v1.integration.WebhookDispatcher.EventType.CEREMONY_COMPLETED,
                        holocron.v1.integration.WebhookDispatcher.WebhookContext(
                            templateId = template.id,
                            templateTitle = template.title,
                            teamName = teamName,
                            totalResponses = totalResponses,
                            totalMembers = totalMembers,
                            frontendUrl = frontendUrl
                        )
                    )
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

        var template = templateCache.get(request.ceremonyTemplateId)
        if (template == null) {
            template = templateRepository.findById(request.ceremonyTemplateId)
                ?: throw StatusException(Status.NOT_FOUND.withDescription("Template not found"))
            templateCache.put(template.id, template, Duration.ofMinutes(30))
        }
            
        if (template.creatorId != userEmail && !template.sharedWithEmailsList.contains(userEmail)) {
            throw StatusException(Status.PERMISSION_DENIED.withDescription("You do not have permission to view these responses"))
        }

        var responses = responseRepository.findByTemplateId(
            request.ceremonyTemplateId,
            if (request.hasFilterStartDate()) request.filterStartDate else null,
            if (request.hasFilterEndDate()) request.filterEndDate else null
        )

        // Facilitation Mode Anonymization
        if (template.hasFacilitationSettings() && template.facilitationSettings.isAnonymized) {
            val isLeader = teamRepository.getMembership(template.teamId, userEmail)?.role == TeamMembership.Role.ROLE_LEADER
            if (!isLeader) {
                responses = responses.map { response ->
                    response.toBuilder().setUserId("anonymous").build()
                }
            }
        }

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
        val filename = NanoId.generate(12, "23456789abcdefghjkmnpqrstuvwxyz")
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
    val mongoUri = System.getenv("MONGODB_URI") ?: "mongodb://localhost:27017"
    val mongoClient = MongoClient.create(mongoUri)
    val templateRepository = CeremonyTemplateRepository(mongoClient)
    val responseRepository = CeremonyResponseRepository(mongoClient)
    val userRepository = UserRepository(mongoClient)
    val teamRepository = TeamRepository(mongoClient)
    
    // Initialize standard storage adapter (Memory by default)
    val storageProvider = StorageFactory.createActiveProvider()

    // 0. Database Seeder
    val seeder = DatabaseSeeder(userRepository, teamRepository, templateRepository, responseRepository)
    runBlocking {
        seeder.seedIfEmpty()
    }

    // Initialize Caches
    val userCache: CachePort<String, User> = CacheFactory.createCache()
    val teamCache: CachePort<String, Team> = CacheFactory.createCache()
    val templateCache: CachePort<String, CeremonyTemplate> = CacheFactory.createCache()

    // 1. Configure the gRPC Service
    val grpcService = GrpcService.builder()
        .addService(CeremonyServiceImpl(templateRepository, responseRepository, teamRepository, templateCache))
        .addService(UserServiceImpl(userRepository, userCache))
        .addService(TeamServiceImpl(teamRepository, userRepository, teamCache))
        .addService(AnalyticsServiceImpl(templateRepository, responseRepository, teamRepository))
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
        // Mount Angular Frontend
        .serviceUnder("/", com.linecorp.armeria.server.file.FileService.builder(
            java.nio.file.Paths.get("frontend-build")
        ).serveCompressedFiles(true)
         .build()
         .orElse(com.linecorp.armeria.server.file.HttpFile.of(
            java.nio.file.Paths.get("frontend-build/index.html")
        ).asService()))
        .build()

    server.start().join()
    println("✅ Armeria Server running on port 8080")
    println("🔍 API Explorer available at: http://localhost:8080/docs")
}

