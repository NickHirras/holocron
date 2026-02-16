package io.holocron.ui;

import io.holocron.team.Team;
import io.holocron.team.TeamMember;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

@QuarkusTest
public class ProfileControllerTest {

        @BeforeEach
        @Transactional
        void setup() {
                io.holocron.ceremony.CeremonyAnswer.deleteAll();
                io.holocron.ceremony.CeremonyResponse.deleteAll();
                io.holocron.ceremony.CeremonyQuestion.deleteAll();
                io.holocron.ceremony.Ceremony.deleteAll();
                TeamMember.deleteAll();
                Team.deleteAll();
                Team redSquadron = new Team();
                redSquadron.name = "Red Squadron";
                redSquadron.timezoneId = "UTC";
                redSquadron.persist();
        }

        @Test
        @TestSecurity(user = "test@example.com", roles = "Operative")
        public void testProfilePageInit() {
                given()
                                .when().get("/profile")
                                .then()
                                .statusCode(200)
                                .body(containsString("test@example.com"))
                                .body(containsString("Red Squadron")) // In available teams
                                .body(containsString("// AVAILABLE SECTORS"));
        }

        @Test
        @TestSecurity(user = "joiner@example.com", roles = "Operative")
        public void testJoinTeam() {
                Team team = Team.find("name", "Red Squadron").firstResult();

                // 1. Initial State: Team is available, not joined
                given()
                                .when().get("/profile")
                                .then()
                                .body(containsString("Red Squadron"))
                                .body(containsString("Join"));

                // 2. Join Team
                given()
                                .formParam("teamId", team.id)
                                .when().post("/profile/join")
                                .then()
                                .statusCode(200); // 303 Redirect followed by 200 OK

                // 3. Verify Membership
                given()
                                .when().get("/profile")
                                .then()
                                .statusCode(200)
                                .body(containsString("Red Squadron"))
                                .body(containsString("// CURRENT ASSIGNMENTS"))
                                .body(containsString("No additional sectors available"));
        }

        @Test
        @TestSecurity(user = "updater@example.com", roles = "Operative")
        public void testUpdateProfile() {
                given()
                                .formParam("name", "Commander Skywalker")
                                .when().post("/profile/update")
                                .then()
                                .statusCode(200);

                given()
                                .when().get("/profile")
                                .then()
                                .statusCode(200)
                                .body(containsString("Commander Skywalker"));
        }
}
