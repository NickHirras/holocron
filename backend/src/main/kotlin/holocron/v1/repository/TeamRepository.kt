package holocron.v1.repository

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import holocron.v1.Team
import holocron.v1.TeamMembership
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import io.viascom.nanoid.NanoId

class TeamRepository(mongoClient: MongoClient) {
    private val database = mongoClient.getDatabase("holocron")
    private val teamCollection: MongoCollection<Document> = database.getCollection("teams")
    private val membershipCollection: MongoCollection<Document> = database.getCollection("team_memberships")

    suspend fun createTeam(displayName: String, creatorUserId: String): Pair<Team, TeamMembership> {
        val now = com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(System.currentTimeMillis() / 1000)
            .build()
            
        val team = Team.newBuilder()
            .setId(NanoId.generate(12, "23456789abcdefghjkmnpqrstuvwxyz"))
            .setDisplayName(displayName)
            .setCreatedAt(now)
            .build()

        val teamDoc = Document("_id", team.id)
            .append("blob", team.toByteArray())
        teamCollection.insertOne(teamDoc)

        val membership = TeamMembership.newBuilder()
            .setTeamId(team.id)
            .setUserId(creatorUserId)
            .setRole(TeamMembership.Role.ROLE_LEADER)
            .build()

        val membershipDoc = Document()
            .append("teamId", membership.teamId)
            .append("userId", membership.userId)
            .append("blob", membership.toByteArray())
            
        membershipCollection.insertOne(membershipDoc)

        return Pair(team, membership)
    }

    suspend fun joinTeam(teamId: String, userId: String): TeamMembership {
        // check if membership already exists
        val existingDoc = membershipCollection.find(
            and(eq("teamId", teamId), eq("userId", userId))
        ).firstOrNull()

        if (existingDoc != null) {
            val blob = existingDoc.get("blob", org.bson.types.Binary::class.java).data
            return TeamMembership.parseFrom(blob)
        }

        val membership = TeamMembership.newBuilder()
            .setTeamId(teamId)
            .setUserId(userId)
            .setRole(TeamMembership.Role.ROLE_MEMBER)
            .build()

        val membershipDoc = Document()
            .append("teamId", membership.teamId)
            .append("userId", membership.userId)
            .append("blob", membership.toByteArray())
            
        membershipCollection.insertOne(membershipDoc)

        return membership
    }

    suspend fun getUserMemberships(userId: String): List<TeamMembership> {
        return membershipCollection.find(eq("userId", userId))
            .toList()
            .mapNotNull { doc ->
                val blob = doc.get("blob", org.bson.types.Binary::class.java)?.data ?: return@mapNotNull null
                TeamMembership.parseFrom(blob)
            }
    }

    suspend fun getTeamMemberships(teamId: String): List<TeamMembership> {
        return membershipCollection.find(eq("teamId", teamId))
            .toList()
            .mapNotNull { doc ->
                val blob = doc.get("blob", org.bson.types.Binary::class.java)?.data ?: return@mapNotNull null
                TeamMembership.parseFrom(blob)
            }
    }

    suspend fun getTeams(teamIds: List<String>): List<Team> {
        if (teamIds.isEmpty()) return emptyList()
        return teamCollection.find(com.mongodb.client.model.Filters.`in`("_id", teamIds))
            .toList()
            .mapNotNull { doc ->
                val blob = doc.get("blob", org.bson.types.Binary::class.java)?.data ?: return@mapNotNull null
                Team.parseFrom(blob)
            }
    }

    suspend fun getMembership(teamId: String, userId: String): TeamMembership? {
        val doc = membershipCollection.find(
            and(eq("teamId", teamId), eq("userId", userId))
        ).firstOrNull() ?: return null
        
        val blob = doc.get("blob", org.bson.types.Binary::class.java)?.data ?: return null
        return TeamMembership.parseFrom(blob)
    }

    suspend fun updateRole(teamId: String, userId: String, role: TeamMembership.Role): TeamMembership? {
        val existingDoc = membershipCollection.find(
            and(eq("teamId", teamId), eq("userId", userId))
        ).firstOrNull() ?: return null

        val blob = existingDoc.get("blob", org.bson.types.Binary::class.java)?.data ?: return null
        val existingMembership = TeamMembership.parseFrom(blob)

        val updatedMembership = existingMembership.toBuilder()
            .setRole(role)
            .build()

        val updatedDoc = Document()
            .append("teamId", updatedMembership.teamId)
            .append("userId", updatedMembership.userId)
            .append("blob", updatedMembership.toByteArray())

        membershipCollection.replaceOne(
            and(eq("teamId", teamId), eq("userId", userId)),
            updatedDoc
        )

        return updatedMembership
    }
}
