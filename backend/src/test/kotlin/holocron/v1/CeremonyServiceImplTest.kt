package holocron.v1

import com.linecorp.armeria.server.ServiceRequestContext
import holocron.v1.repository.CeremonyResponseRepository
import holocron.v1.repository.CeremonyTemplateRepository
import holocron.v1.repository.TeamRepository
import io.grpc.StatusException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CeremonyServiceImplTest {

    @Test
    fun `test updateCeremonyTemplate denies non-leader`() = runTest {
        val templateRepo = mockk<CeremonyTemplateRepository>()
        val responseRepo = mockk<CeremonyResponseRepository>()
        val teamRepo = mockk<TeamRepository>()

        val service = CeremonyServiceImpl(templateRepo, responseRepo, teamRepo)

        val ctx = mockk<ServiceRequestContext>()
        every { ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR) } returns "user@example.com"
        
        mockkStatic(ServiceRequestContext::class)
        every { ServiceRequestContext.current() } returns ctx

        val existingTemplate = CeremonyTemplate.newBuilder()
            .setId("t1")
            .setTeamId("team1")
            .build()

        coEvery { templateRepo.findById("t1") } returns existingTemplate
        coEvery { teamRepo.getMembership("team1", "user@example.com") } returns TeamMembership.newBuilder()
            .setRole(TeamMembership.Role.ROLE_MEMBER)
            .build()

        val request = UpdateCeremonyTemplateRequest.newBuilder()
            .setTemplate(CeremonyTemplate.newBuilder().setId("t1").build())
            .build()

        try {
            service.updateCeremonyTemplate(request)
        } catch (e: StatusException) {
            assertEquals("Only team leaders can update templates", e.status.description)
        }
    }

    @Test
    fun `test listCeremonyResponses anonymizes for non-leader`() = runTest {
        val templateRepo = mockk<CeremonyTemplateRepository>()
        val responseRepo = mockk<CeremonyResponseRepository>()
        val teamRepo = mockk<TeamRepository>()

        val service = CeremonyServiceImpl(templateRepo, responseRepo, teamRepo)

        val ctx = mockk<ServiceRequestContext>()
        every { ctx.attr(MockAuthDecorator.USER_EMAIL_ATTR) } returns "user@example.com"
        
        mockkStatic(ServiceRequestContext::class)
        every { ServiceRequestContext.current() } returns ctx

        val template = CeremonyTemplate.newBuilder()
            .setId("t1")
            .setTeamId("team1")
            .setCreatorId("other@example.com")
            .addSharedWithEmails("user@example.com")
            .setFacilitationSettings(FacilitationSettings.newBuilder().setIsAnonymized(true))
            .build()

        coEvery { templateRepo.findById("t1") } returns template
        coEvery { teamRepo.getMembership("team1", "user@example.com") } returns TeamMembership.newBuilder()
            .setRole(TeamMembership.Role.ROLE_MEMBER)
            .build()

        val response = CeremonyResponse.newBuilder()
            .setUserId("real_user@example.com")
            .build()

        coEvery { responseRepo.findByTemplateId("t1", null, null) } returns listOf(response)

        val request = ListCeremonyResponsesRequest.newBuilder()
            .setCeremonyTemplateId("t1")
            .build()

        val result = service.listCeremonyResponses(request)
        assertEquals(1, result.responsesCount)
        assertEquals("anonymous", result.responsesList[0].userId)
    }
}
