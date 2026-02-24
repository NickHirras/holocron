package holocron.v1

import com.linecorp.armeria.server.ServiceRequestContext
import holocron.v1.repository.UserRepository
import io.grpc.Status
import io.grpc.StatusException

class UserServiceImpl(
    private val repository: UserRepository
) : UserServiceGrpcKt.UserServiceCoroutineImplBase() {

    override suspend fun getSelf(request: GetSelfRequest): GetSelfResponse {
        val ctx = ServiceRequestContext.current()
        val email = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
        
        if (email == null) {
            throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing x-mock-user-id header"))
        }

        val user = repository.findOrCreate(email)
        
        return GetSelfResponse.newBuilder()
            .setUser(user)
            .build()
    }
}
