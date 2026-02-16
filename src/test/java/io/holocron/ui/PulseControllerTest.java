package io.holocron.ui;

import io.holocron.audit.AuditEntry;
import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyType;
import io.holocron.pulse.PulseService;
import io.holocron.team.Team;
import io.holocron.user.User;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class PulseControllerTest {

    @Inject
    PulseService pulseService;

    @Inject
    UserTransaction userTransaction;

    @BeforeEach
    @Transactional
    void setup() {
        // Ensure we have data
        User alice = User.findByEmail("alice@holocron.io");
        if (alice == null) {
            alice = new User();
            alice.email = "alice@holocron.io";
            alice.name = "Alice";
            alice.role = "LEAD";
            alice.persist();
        }

        Team eng = Team.find("name", "Engineering").firstResult();
        if (eng == null) {
            eng = new Team();
            eng.name = "Engineering";
            eng.persist();
        }
    }

    @Test
    public void testUnauthorizedAccess() {
        given()
                .when().get("/pulse")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "LEAD")
    public void testAuthorizedAccess() {
        given()
                .when().get("/pulse")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "unknown@holocron.io", roles = "user")
    public void testUnknownUserAccess() {
        given()
                .redirects().follow(false)
                .when().get("/pulse/1")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "LEAD")
    public void testAuditEntryCreatedOnSubmit() throws Exception {
        // Manually manage transaction to avoid locking issues with SQLite + RestAssured
        userTransaction.begin();
        Team team = Team.find("name", "Engineering").firstResult();
        Ceremony c = new Ceremony();
        c.team = team;
        c.type = CeremonyType.PULSE;
        c.isActive = true;
        c.title = "Weekly Pulse";
        c.persist();
        userTransaction.commit();

        try {
            // Submit a response
            given()
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("comments", "Doing great!")
                    .redirects().follow(false)
                    .when().post("/pulse/" + team.id)
                    .then()
                    .statusCode(303);

            // Verify Audit Entry
            long count = AuditEntry.find("action = ?1 and who = ?2", "SUBMIT_PULSE", "alice@holocron.io").count();
            assert count > 0 : "Audit entry not found for alice@holocron.io";
        } finally {
            // Cleanup
            userTransaction.begin();
            Ceremony.delete("title", "Weekly Pulse");
            AuditEntry.delete("action", "SUBMIT_PULSE");
            userTransaction.commit();
        }
    }
}
