package holocron.v1.repository

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import holocron.v1.TeamMembership
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.bson.Document
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TeamRepositoryTest {

    @Test
    fun `test createTeam`() = runTest {
        val mongoClient = mockk<MongoClient>()
        val database = mockk<MongoDatabase>()
        val teamCollection = mockk<MongoCollection<Document>>()
        val membershipCollection = mockk<MongoCollection<Document>>()

        every { mongoClient.getDatabase("holocron") } returns database
        every { database.getCollection("teams", Document::class.java) } returns teamCollection
        every { database.getCollection("team_memberships", Document::class.java) } returns membershipCollection
        // Or if it's database.getCollection<Document>("teams")
        every { database.getCollection<Document>("teams") } returns teamCollection
        every { database.getCollection<Document>("team_memberships") } returns membershipCollection

        coEvery { teamCollection.insertOne(any<Document>(), any()) } returns mockk()
        coEvery { membershipCollection.insertOne(any<Document>(), any()) } returns mockk()

        val repository = TeamRepository(mongoClient)

        val (team, membership) = repository.createTeam("My Test Team", "user123")

        assertEquals("My Test Team", team.displayName)
        assertNotNull(team.id)
        
        assertEquals(team.id, membership.teamId)
        assertEquals("user123", membership.userId)
        assertEquals(TeamMembership.Role.ROLE_LEADER, membership.role)
    }
}
