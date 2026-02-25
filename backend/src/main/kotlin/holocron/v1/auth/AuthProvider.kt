package holocron.v1.auth

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.linecorp.armeria.client.WebClient
import com.linecorp.armeria.common.HttpMethod
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.MediaType
import kotlinx.coroutines.future.await

interface AuthProvider {
    val id: String
    fun getLoginUrl(emailHint: String? = null): String
    suspend fun exchangeCode(code: String): String
}

class MockAuthProvider : AuthProvider {
    override val id = "mock"

    override fun getLoginUrl(emailHint: String?): String {
        val email = emailHint ?: "mockuser@example.com"
        return "/api/auth/callback/mock?code=${java.net.URLEncoder.encode(email, "UTF-8")}"
    }

    override suspend fun exchangeCode(code: String): String {
        return code // The code itself is the mock email
    }
}

class GoogleAuthProvider(
    private val clientId: String,
    private val clientSecret: String,
    private val issuer: String? = null
) : AuthProvider {
    override val id = "google"
    private val redirectUri = "http://localhost:8080/api/auth/callback/google"
    private val mapper = jacksonObjectMapper()

    private val cleanIssuer = issuer?.removeSuffix("/")
    private val authUrl = cleanIssuer?.let { "$it/authorize" } ?: "https://accounts.google.com/o/oauth2/v2/auth"
    private val tokenUrl = cleanIssuer?.let { "$it/oauth/token" } ?: "https://oauth2.googleapis.com/token"
    private val userInfoUrl = cleanIssuer?.let { "$it/userinfo" } ?: "https://www.googleapis.com/oauth2/v2/userinfo"

    override fun getLoginUrl(emailHint: String?): String {
        return "$authUrl?" +
                "client_id=$clientId&" +
                "redirect_uri=$redirectUri&" +
                "response_type=code&" +
                "scope=email profile" +
                (if (emailHint != null) "&login_hint=${java.net.URLEncoder.encode(emailHint, "UTF-8")}" else "")
    }

    override suspend fun exchangeCode(code: String): String {
        val client = WebClient.of()
        
        val tokenReq = HttpRequest.builder()
            .post(tokenUrl)
            .content(
                MediaType.FORM_DATA,
                "client_id=$clientId&client_secret=$clientSecret&code=$code&grant_type=authorization_code&redirect_uri=$redirectUri"
            )
            .build()
            
        val tokenResponse = client.execute(tokenReq).aggregate().await()
        val tokenResponseBody = tokenResponse.contentUtf8()
        
        val tokenData = mapper.readValue<Map<String, Any>>(tokenResponseBody)
        val accessToken = tokenData["access_token"] as? String
            ?: throw RuntimeException("Failed to get Google access token: $tokenResponseBody")

        val userReq = HttpRequest.builder()
            .get(userInfoUrl)
            .header("Authorization", "Bearer $accessToken")
            .build()

        val userResponse = client.execute(userReq).aggregate().await()
        val userResponseBody = userResponse.contentUtf8()
        
        val userData = mapper.readValue<Map<String, Any>>(userResponseBody)
        return userData["email"] as? String
            ?: throw RuntimeException("Failed to get Google user email")
    }
}

class GithubAuthProvider(
    private val clientId: String,
    private val clientSecret: String
) : AuthProvider {
    override val id = "github"
    private val mapper = jacksonObjectMapper()

    override fun getLoginUrl(emailHint: String?): String {
        return "https://github.com/login/oauth/authorize?client_id=$clientId&scope=user:email" +
                (if (emailHint != null) "&login=${java.net.URLEncoder.encode(emailHint, "UTF-8")}" else "")
    }

    override suspend fun exchangeCode(code: String): String {
        val client = WebClient.of()

        val tokenReq = HttpRequest.builder()
            .post("https://github.com/login/oauth/access_token")
            .header("Accept", "application/json")
            .content(
                MediaType.FORM_DATA,
                "client_id=$clientId&client_secret=$clientSecret&code=$code"
            )
            .build()
            
        val tokenResponse = client.execute(tokenReq).aggregate().await()
        val tokenResponseBody = tokenResponse.contentUtf8()
        
        val tokenData = mapper.readValue<Map<String, Any>>(tokenResponseBody)
        val accessToken = tokenData["access_token"] as? String
            ?: throw RuntimeException("Failed to get GitHub access token: $tokenResponseBody")

        val userReq = HttpRequest.builder()
            .get("https://api.github.com/user/emails")
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/json")
            // required by GitHub API
            .header("User-Agent", "Holocron-App") 
            .build()

        val userResponse = client.execute(userReq).aggregate().await()
        val userResponseBody = userResponse.contentUtf8()
        
        val emails = mapper.readValue<List<Map<String, Any>>>(userResponseBody)
        val primaryEmail = emails.find { it["primary"] == true }?.get("email") as? String
        return primaryEmail ?: throw RuntimeException("Failed to get primary GitHub email")
    }
}
