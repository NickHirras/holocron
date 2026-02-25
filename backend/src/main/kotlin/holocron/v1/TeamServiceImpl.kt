package holocron.v1

import holocron.v1.repository.TeamRepository
import io.grpc.Status
import io.grpc.StatusException

class TeamServiceImpl(private val teamRepository: TeamRepository) : TeamServiceGrpcKt.TeamServiceCoroutineImplBase() {
    override suspend fun createTeam(request: CreateTeamRequest): CreateTeamResponse {
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
            ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing user authentication"))

        val (team, membership) = teamRepository.createTeam(request.displayName, userEmail)
        
        return CreateTeamResponse.newBuilder()
            .setTeam(team)
            .setMembership(membership)
            .build()
    }

    override suspend fun joinTeam(request: JoinTeamRequest): JoinTeamResponse {
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
            ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing user authentication"))

        val membership = teamRepository.joinTeam(request.teamId, userEmail)
        return JoinTeamResponse.newBuilder().setMembership(membership).build()
    }

    override suspend fun listMyTeams(request: ListMyTeamsRequest): ListMyTeamsResponse {
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
            ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing user authentication"))

        val memberships = teamRepository.getUserMemberships(userEmail)
        val teamIds = memberships.map { it.teamId }
        val teams = teamRepository.getTeams(teamIds)

        return ListMyTeamsResponse.newBuilder()
            .addAllTeams(teams)
            .addAllMemberships(memberships)
            .build()
    }
}
