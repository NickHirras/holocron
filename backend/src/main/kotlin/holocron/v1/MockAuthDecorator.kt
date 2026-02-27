package holocron.v1

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.server.DecoratingHttpServiceFunction
import com.linecorp.armeria.server.HttpService
import com.linecorp.armeria.server.ServiceRequestContext
import io.netty.util.AttributeKey

class MockAuthDecorator : DecoratingHttpServiceFunction {
    companion object {
        val USER_EMAIL_ATTR: AttributeKey<String> = AttributeKey.valueOf("USER_EMAIL")
        val FRONTEND_URL_ATTR: AttributeKey<String> = AttributeKey.valueOf("FRONTEND_URL")
    }

    private val jwtAlgorithm: Algorithm

    init {
        val jwtSecret = System.getenv("JWT_SECRET") ?: "holocron-secret-key-development-only"
        jwtAlgorithm = Algorithm.HMAC256(jwtSecret)
    }

    override fun serve(delegate: HttpService, ctx: ServiceRequestContext, req: HttpRequest): HttpResponse {
        val authHeader = req.headers().get("authorization") ?: req.headers().get("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ", ignoreCase = true)) {
            val token = authHeader.substring(7)
            try {
                val verifier = JWT.require(jwtAlgorithm)
                    .withIssuer("holocron")
                    .build()
                val decodedJWT = verifier.verify(token)
                val email = decodedJWT.subject
                if (email != null) {
                    ctx.setAttr(USER_EMAIL_ATTR, email)
                }
            } catch (e: Exception) {
                println("⚠️ JWT Verification failed: ${e.message}")
                e.printStackTrace()
            }
        }
        
        // Derive Frontend URL from Env, fallback to Origin, fallback to Host
        var frontendUrl = System.getenv("FRONTEND_URL")
        if (frontendUrl == null) {
            frontendUrl = req.headers().get("origin")
        }
        if (frontendUrl == null) {
            val host = req.headers().get("host")
            if (host != null) {
                val scheme = req.scheme() ?: "http"
                frontendUrl = "$scheme://$host"
            }
        }
        if (frontendUrl == null) {
            frontendUrl = "http://localhost:4200"
        }
        ctx.setAttr(FRONTEND_URL_ATTR, frontendUrl)

        return delegate.serve(ctx, req)
    }
}
