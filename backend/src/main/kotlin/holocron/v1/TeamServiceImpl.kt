package holocron.v1

import holocron.v1.repository.TeamRepository
import holocron.v1.repository.UserRepository
import io.grpc.Status
import io.grpc.StatusException

import holocron.v1.cache.CachePort
import java.time.Duration

class TeamServiceImpl(
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository,
    private val teamCache: CachePort<String, Team>
) : TeamServiceGrpcKt.TeamServiceCoroutineImplBase() {
    override suspend fun createTeam(request: CreateTeamRequest): CreateTeamResponse {
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
            ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing user authentication"))

        val (team, membership) = teamRepository.createTeam(request.displayName, userEmail)
        
        // Cache the newly created team
        teamCache.put(team.id, team, Duration.ofMinutes(30))
        
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
        
        val cachedTeams = teamIds.mapNotNull { teamCache.get(it) }
        val missingTeamIds = teamIds.filter { id -> cachedTeams.none { it.id == id } }
        
        val dbTeams = teamRepository.getTeams(missingTeamIds)
        dbTeams.forEach { teamCache.put(it.id, it, Duration.ofMinutes(30)) }

        val teams = cachedTeams + dbTeams

        return ListMyTeamsResponse.newBuilder()
            .addAllTeams(teams)
            .addAllMemberships(memberships)
            .build()
    }

    override suspend fun getTeamRoster(request: GetTeamRosterRequest): GetTeamRosterResponse {
        val ctx = com.linecorp.armeria.server.ServiceRequestContext.current()
        val userEmail = ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR)
            ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("Missing user authentication"))

        val myMembership = teamRepository.getMembership(request.teamId, userEmail)
            ?: throw StatusException(Status.PERMISSION_DENIED.withDescription("You are not a member of this team"))

        val memberships = teamRepository.getTeamMemberships(request.teamId)
        val emails = memberships.map { it.userId } // assuming userId stores email
        val users = userRepository.getUsers(emails)
        val usersByEmail = users.associateBy { it.email }

        val teamMembers = memberships.map { membership ->
            val user = usersByEmail[membership.userId] ?: User.newBuilder().setEmail(membership.userId).build()
            TeamMember.newBuilder()
                .setUser(user)
                .setRole(membership.role)
                .build()
        }

        return GetTeamRosterResponse.newBuilder()
            .addAllMembers(teamMembers)
            .build()
    }
}
