package io.holocron.ui;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyType;
import io.holocron.team.Team;
import io.holocron.team.TeamMember;
import io.holocron.user.User;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class ReportControllerTest {

    @BeforeEach
    @Transactional
    void setup() {
        io.holocron.ceremony.CeremonyResponse.deleteAll();
        io.holocron.ceremony.Ceremony.deleteAll();
        io.holocron.team.TeamMember.deleteAll();
        io.holocron.team.Team.deleteAll();
        io.holocron.user.User.deleteAll();

        Team team = new Team();
        team.name = "Gold Squadron";
        team.timezoneId = "UTC";
        team.persist();

        User user = new User();
        user.email = "leader@example.com";
        user.name = "Gold Leader";
        user.role = "Operative";
        user.persist();

        TeamMember member = new TeamMember();
        member.team = team;
        member.user = user;
        member.role = "Leader";
        member.persist();

        Ceremony pulse = new Ceremony();
        pulse.team = team;
        pulse.type = CeremonyType.PULSE;
        pulse.isActive = true;
        pulse.title = "Daily Briefing";
        pulse.persist();
    }

    @Test
    @TestSecurity(user = "leader@example.com", roles = "Operative")
    public void testDailyRollup() {
        Team team = Team.find("name", "Gold Squadron").firstResult();

        given()
                .when().get("/reports/daily/" + team.id)
                .then()
                .statusCode(200)
                .body(containsString("DAILY ROLLUP"))
                .body(containsString("Gold Squadron"))
                .body(containsString("AWOL")); // Leader hasn't submitted yet
    }
}
