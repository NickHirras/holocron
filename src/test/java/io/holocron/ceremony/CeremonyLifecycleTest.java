package io.holocron.ceremony;

import io.holocron.audit.AuditEntry;
import io.holocron.service.ScheduleService;
import io.holocron.team.Team;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class CeremonyLifecycleTest {

    @Inject
    ScheduleService scheduleService;

    @Inject
    TransactionalWrapper transaction;

    @Test
    @TestSecurity(user = "commander", roles = { "Team Lead" })
    public void testCeremonyCreationAndAudit() {
        // 1. Setup Team
        transaction.run(() -> {
            if (Team.findById(1L) == null) {
                Team team = new Team();
                team.name = "Rogue Squadron";
                team.persist();
            }
        });

        // 2. Create Ceremony via API
        Ceremony ceremony = new Ceremony();
        ceremony.title = "Attack Run Briefing";
        ceremony.description = "Target the exhaust port.";
        ceremony.rrule = "FREQ=DAILY;BYHOUR=9";
        ceremony.timezone = "UTC";
        ceremony.isActive = true;
        ceremony.type = CeremonyType.STANDUP;
        Team team = new Team();
        team.id = 1L;
        ceremony.team = team;

        Integer ceremonyId = given()
                .contentType("application/json")
                .body(ceremony)
                .when()
                .post("/api/ceremonies")
                .then()
                .statusCode(201)
                .body("title", equalTo("Attack Run Briefing"))
                .extract().path("id");

        // 3. Verify Audit Log
        transaction.run(() -> {
            Assertions.assertTrue(AuditEntry.count("action", "create_ceremony") > 0, "Audit entry should exist");
        });

        // 4. Test Scheduling (Manually trigger)
        transaction.run(() -> {
            scheduleService.triggerPulse(ceremonyId.longValue());
            // Verify placeholders? PulseService createPulse logic.
            // Since we don't have users in the team in this test setup, no placeholders
            // created,
            // but we verify no exception is thrown and logic executes.
        });
    }

    @Test
    @TestSecurity(user = "trooper", roles = { "Member" })
    public void testRBAC() {
        Ceremony ceremony = new Ceremony();
        ceremony.title = "Unauthorized Ritual";

        given()
                .contentType("application/json")
                .body(ceremony)
                .when()
                .post("/api/ceremonies")
                .then()
                .statusCode(403);
    }
}
