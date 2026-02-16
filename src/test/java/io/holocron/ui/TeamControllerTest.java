package io.holocron.ui;

import io.holocron.team.Team;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

@QuarkusTest
public class TeamControllerTest {

    @BeforeEach
    @Transactional
    void setup() {
        io.holocron.team.TeamMember.deleteAll();
        Team.deleteAll();
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = "Operative")
    public void testCreateTeam() {
        given()
                .formParam("name", "Black Squadron")
                .formParam("timezoneId", "UTC")
                .when().post("/teams")
                .then()
                .statusCode(200); // Redirect followed

        given()
                .when().get("/teams")
                .then()
                .statusCode(200)
                .body(containsString("Black Squadron"));
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = "Operative")
    public void testEditTeam() {
        Team team = new Team();
        team.name = "Gold Squadron";
        team.timezoneId = "UTC";
        createTeam(team);

        given()
                .when().get("/teams/" + team.id + "/edit")
                .then()
                .statusCode(200)
                .body(containsString("Gold Squadron"));

        given()
                .formParam("name", "Gold Leader")
                .formParam("timezoneId", "EST")
                .when().post("/teams/" + team.id)
                .then()
                .statusCode(200);

        given()
                .when().get("/teams")
                .then()
                .statusCode(200)
                .body(containsString("Gold Leader"))
                .body(not(containsString("Gold Squadron")));
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = "Operative")
    public void testDeleteTeam() {
        Team team = new Team();
        team.name = "Rogue Squadron";
        team.timezoneId = "UTC";
        createTeam(team);

        given()
                .when().get("/teams")
                .then()
                .body(containsString("Rogue Squadron"));

        given()
                .when().post("/teams/" + team.id + "/delete")
                .then()
                .statusCode(200);

        given()
                .when().get("/teams")
                .then()
                .body(not(containsString("Rogue Squadron")));
    }

    @Transactional
    void createTeam(Team team) {
        team.persist();
    }
}
