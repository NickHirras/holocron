package io.holocron.report;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyResponse;
import io.holocron.ceremony.CeremonyType;
import io.holocron.report.OverseerService.OperativeStatus;
import io.holocron.team.Team;
import io.holocron.team.TeamMember;
import io.holocron.user.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class OverseerServiceTest {

    @Inject
    OverseerService overseerService;

    @Test
    @TestTransaction
    public void testDashboardAggregation() {
        // 1. Setup Data
        User lead = new User();
        lead.email = "lead@test.com";
        lead.name = "Lead User";
        lead.persist();

        User member1 = new User();
        member1.email = "m1@test.com";
        member1.name = "Member One";
        member1.persist();

        User member2 = new User();
        member2.email = "m2@test.com";
        member2.name = "Member Two";
        member2.persist();

        Team team = new Team();
        team.name = "Alpha Squad";
        team.persist();

        TeamMember tm1 = new TeamMember();
        tm1.user = lead;
        tm1.team = team;
        tm1.role = "Lead";
        tm1.persist();

        TeamMember tm2 = new TeamMember();
        tm2.user = member1;
        tm2.team = team;
        tm2.role = "Operative";
        tm2.persist();

        TeamMember tm3 = new TeamMember();
        tm3.user = member2;
        tm3.team = team;
        tm3.role = "Operative";
        tm3.persist();

        // 2. Create Active Pulse
        Ceremony pulse = new Ceremony();
        pulse.team = team;
        pulse.type = CeremonyType.PULSE;
        pulse.isActive = true;
        pulse.persist();

        // 3. Submit Response for Member 1
        CeremonyResponse response = new CeremonyResponse();
        response.ceremony = pulse;
        response.user = member1;
        response.date = LocalDate.now();
        response.submittedAt = LocalDateTime.now();
        response.comments = "All clear.";
        response.persist();

        // 4. Test Service
        OverseerService.OverseerDashboardDTO dashboard = overseerService.getDashboardData(lead);

        // 5. Assertions
        assertNotNull(dashboard);
        assertEquals(1, dashboard.teams.size());

        OverseerService.TeamOverviewDTO teamOverview = dashboard.teams.get(0);
        assertEquals("Alpha Squad", teamOverview.teamName);
        // Should have 3 operatives (Lead + 2 Members) based on current implementation
        assertEquals(3, teamOverview.operatives.size());

        // Verify Member 1 (Submitted)
        OverseerService.OperativeStatusDTO op1 = teamOverview.operatives.stream()
                .filter(o -> o.userId.equals(member1.id))
                .findFirst().orElseThrow();
        assertEquals(OperativeStatus.SUBMITTED, op1.status);
        assertEquals("All clear.", op1.latestResponseSnippet);

        // Verify Member 2 (Missing)
        OverseerService.OperativeStatusDTO op2 = teamOverview.operatives.stream()
                .filter(o -> o.userId.equals(member2.id))
                .findFirst().orElseThrow();
        assertEquals(OperativeStatus.MISSING, op2.status);
    }
}
