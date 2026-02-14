package io.holocron.pulse;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyType;
import io.holocron.team.Team;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class PulseTest {

    private Long teamId;

    @BeforeEach
    @Transactional
    void setup() {
        // Cleanup first to avoid unique constraint issues if possible, though
        // @Transactional tests usually rollback
        // But QuarkusTest wraps entire test class? No, per method.
        // However, standard @QuarkusTest runs against the same DB as dev mode if
        // configured so, or a test container.
        // Usually it uses H2 or Testcontainers.

        Team team = new Team();
        team.name = "Test Team Pulse";
        team.timezoneId = "UTC";
        team.persist();
        teamId = team.id;

        Ceremony pulse = new Ceremony();
        pulse.title = "Test Pulse";
        pulse.team = team;
        pulse.type = CeremonyType.PULSE;
        pulse.isActive = true;
        pulse.persist();

        // Ensure user exists for the mock in Controller
        // The controller hardcodes "alice@holocron.io"
        if (io.holocron.user.User.findByEmail("alice@holocron.io") == null) {
            io.holocron.user.User alice = new io.holocron.user.User();
            alice.email = "alice@holocron.io";
            alice.name = "Alice";
            alice.persist();
        }
    }

    @Test
    public void testPulsePage() {
        given()
                .when().get("/pulse/" + teamId)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body(containsString("Pulse"));
    }

    @Test
    public void testSubmitPulse() {
        given()
                .contentType(ContentType.URLENC)
                .formParam("comments", "Start of a new journey")
                .when().post("/pulse/" + teamId)
                .then()
                .statusCode(200) // Redirect followed
                .body(containsString("Response Recorded"));
    }
}
