package holocron.v1.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.common.MediaType
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.ProducesJson
import java.util.Date

class AuthRestService {
    private val providers = mutableMapOf<String, AuthProvider>()
    private val jwtAlgorithm: Algorithm

    init {
        val jwtSecret = System.getenv("JWT_SECRET") ?: "holocron-secret-key-development-only"
        jwtAlgorithm = Algorithm.HMAC256(jwtSecret)

        providers["mock"] = MockAuthProvider()
        
        val googleClientId = System.getenv("AUTH_GOOGLE_CLIENT_ID")
        val googleClientSecret = System.getenv("AUTH_GOOGLE_CLIENT_SECRET")
        if (!googleClientId.isNullOrBlank() && !googleClientSecret.isNullOrBlank()) {
            providers["google"] = GoogleAuthProvider(googleClientId, googleClientSecret)
        }

        val githubClientId = System.getenv("AUTH_GITHUB_CLIENT_ID")
        val githubClientSecret = System.getenv("AUTH_GITHUB_CLIENT_SECRET")
        if (!githubClientId.isNullOrBlank() && !githubClientSecret.isNullOrBlank()) {
            providers["github"] = GithubAuthProvider(githubClientId, githubClientSecret)
        }
    }

    @Get("/api/auth/providers")
    @ProducesJson
    fun getProviders(): List<String> {
        return providers.keys.toList()
    }

    @Get("/api/auth/login/:provider")
    fun login(@Param("provider") provider: String, ctx: ServiceRequestContext): HttpResponse {
        val authProvider = providers[provider]
        if (authProvider == null) {
            return HttpResponse.of(HttpStatus.NOT_FOUND, MediaType.PLAIN_TEXT_UTF_8, "Provider not found")
        }
        
        val emailHint = ctx.queryParam("email")
        val loginUrl = authProvider.getLoginUrl(emailHint)
        return HttpResponse.ofRedirect(loginUrl)
    }

    @Get("/api/auth/callback/:provider")
    suspend fun callback(@Param("provider") provider: String, ctx: ServiceRequestContext): HttpResponse {
        val code = ctx.queryParam("code")
        if (code == null) {
            return HttpResponse.ofRedirect("http://localhost:4200/login?error=missing_code")
        }

        val authProvider = providers[provider]
        if (authProvider == null) {
            return HttpResponse.ofRedirect("http://localhost:4200/login?error=provider_not_found")
        }

        return try {
            val email = authProvider.exchangeCode(code)
            
            val jwtToken = JWT.create()
                .withIssuer("holocron")
                .withSubject(email)
                .withIssuedAt(Date())
                .withExpiresAt(Date(System.currentTimeMillis() + 86400000L)) // 24 hours
                .sign(jwtAlgorithm)

            HttpResponse.ofRedirect("http://localhost:4200/login?token=$jwtToken")
        } catch (e: Exception) {
            e.printStackTrace()
            HttpResponse.ofRedirect("http://localhost:4200/login?error=exchange_failed")
        }
    }
}
