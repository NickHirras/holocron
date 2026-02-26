package holocron.v1

import holocron.v1.repository.CeremonyResponseRepository
import holocron.v1.repository.CeremonyTemplateRepository
import holocron.v1.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class AnalyticsServiceImplTest {
    @Test
    fun `getTeamHealth calculates metrics correctly`() = runTest {
        val templateRepo = mockk<CeremonyTemplateRepository>()
        val responseRepo = mockk<CeremonyResponseRepository>()
        val teamRepo = mockk<TeamRepository>()

        val service = AnalyticsServiceImpl(templateRepo, responseRepo, teamRepo)

        val teamId = "team1"
        val request = GetTeamHealthRequest.newBuilder()
            .setTeamId(teamId)
            .setStartTime(com.google.protobuf.Timestamp.newBuilder().setSeconds(0).build())
            .setEndTime(com.google.protobuf.Timestamp.newBuilder().setSeconds(Instant.now().epochSecond).build())
            .build()

        // 1. Mock team members (size 2)
        val memberships = listOf(
            TeamMembership.newBuilder().setUserId("user1").build(),
            TeamMembership.newBuilder().setUserId("user2").build()
        )
        coEvery { teamRepo.getTeamMemberships(teamId) } returns memberships

        // 2. Mock templates (2 templates)
        val template1 = CeremonyTemplate.newBuilder().setId("t1").build()
        val template2 = CeremonyTemplate.newBuilder().setId("t2").build()
        coEvery { templateRepo.findByTeamId(teamId) } returns listOf(template1, template2)

        // 3. Mock responses
        // Template 1 has 1 response
        val response1 = CeremonyResponse.newBuilder().putAnswers("q1", Answer.newBuilder().setScaleAnswer(ScaleAnswer.newBuilder().setValue(4)).build()).build()
        coEvery { responseRepo.findByTemplateId("t1", any(), any()) } returns listOf(response1)

        // Template 2 has 2 responses
        val response2 = CeremonyResponse.newBuilder().putAnswers("q2", Answer.newBuilder().setTextAnswer(TextAnswer.newBuilder().setValue("I have a big blocker")).build()).build()
        val response3 = CeremonyResponse.newBuilder().putAnswers("q1", Answer.newBuilder().setScaleAnswer(ScaleAnswer.newBuilder().setValue(5)).build()).build()
        coEvery { responseRepo.findByTemplateId("t2", any(), any()) } returns listOf(response2, response3)

        // Calculate expected metrics:
        // Total responses = 3. Team size = 2. Templates = 2.
        // Participation rate = 3 / (2 * 2) = 0.75 * 100 = 75.0%
        // Sentiment: 4 and 5 -> average = 4.5
        // Blockers: 1

        val response = service.getTeamHealth(request)

        val metrics = response.metricsList.associate { it.metricName to it.value }

        assertEquals(75.0f, metrics["Participation Rate"])
        assertEquals(4.5f, metrics["Sentiment Trend"])
        assertEquals(1.0f, metrics["Blocker Count"])
    }
}
