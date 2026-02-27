package holocron.v1

import com.linecorp.armeria.server.ServiceRequestContext
import holocron.v1.repository.UserRepository
import io.grpc.Status
import io.grpc.StatusException

import java.time.Duration
import holocron.v1.cache.CachePort

class UserServiceImpl(
    private val repository: UserRepository,
    private val userCache: CachePort<String, User>
) : UserServiceGrpcKt.UserServiceCoroutineImplBase() {

    override suspend fun getSelf(request: GetSelfRequest): GetSelfResponse {
        val ctx = ServiceRequestContext.current()
        val email = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
        
        if (email == null) {
            throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing x-mock-user-id header"))
        }

        // 1. Check Cache
        val cachedUser = userCache.get(email)
        if (cachedUser != null) {
            return GetSelfResponse.newBuilder()
                .setUser(cachedUser)
                .build()
        }

        // 2. Fetch/Create from DB
        val user = repository.findOrCreate(email)
        
        // 3. Populate Cache (TTL 10 mins)
        userCache.put(email, user, Duration.ofMinutes(10))
        
        return GetSelfResponse.newBuilder()
            .setUser(user)
            .build()
    }
}
