package io.holocron.ui;

import io.holocron.archive.Artifact;
import io.holocron.archive.ArtifactService;
import io.holocron.ceremony.Ceremony;
import io.holocron.team.Team;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class ArchiveIntegrationTest {

    @Inject
    ArtifactService artifactService;

    @Test
    public void testArtifactGenerationAndRetrieval() {
        // 1. Setup Data (in a separate committed transaction)
        Long[] ids = QuarkusTransaction.requiringNew().call(() -> {
            Team team = new Team();
            team.name = "Rogue Squadron Test";
            team.persist();

            Ceremony ceremony = new Ceremony();
            ceremony.title = "Weekly Debrief Test";
            ceremony.team = team;
            ceremony.persist();

            return new Long[] { team.id, ceremony.id };
        });

        Long teamId = ids[0];
        Long ceremonyId = ids[1];

        // 2. Generate Artifact (also in transaction)
        Long artifactId = QuarkusTransaction.requiringNew().call(() -> {
            Ceremony c = Ceremony.findById(ceremonyId);
            LocalDate today = LocalDate.now();
            Artifact artifact = artifactService.generateArtifact(c, today, today);
            return artifact.id;
        });

        // 3. Test Controller List
        given()
                .when().get("/archives")
                .then()
                .statusCode(200)
                .body(containsString("Weekly Debrief Test"));

        // 4. Test Controller Detail (HTMX)
        given()
                .pathParam("id", artifactId)
                .when().get("/archives/{id}")
                .then()
                .statusCode(200)
                .body(containsString("MISSION DEBRIEF"))
                .body(containsString("ARCHIVE_ID: " + artifactId));

        // Cleanup? With sqlite in test, maybe not strictly needed if tests are isolated
        // or db is recreated.
        // But good practice if shared db.
    }
}
