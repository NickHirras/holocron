package io.holocron.ui;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyQuestion;
import io.holocron.ceremony.CeremonyType;
import io.holocron.team.Team;
import io.holocron.user.User;
import io.holocron.user.UserStats;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class CeremonyControllerTest {

    @Inject
    UserTransaction userTransaction;

    @BeforeEach
    @Transactional
    void setup() {
        User alice = User.findByEmail("alice@holocron.io");
        if (alice == null) {
            alice = new User();
            alice.email = "alice@holocron.io";
            alice.name = "Alice";
            alice.role = "LEAD";
            alice.persist();

            UserStats stats = new UserStats();
            stats.user = alice;
            stats.persist();
        }

        Team eng = Team.find("name", "Engineering").firstResult();
        if (eng == null) {
            eng = new Team();
            eng.name = "Engineering";
            eng.persist();
        }
    }

    @Test
    public void testUnauthenticatedAccess() {
        given()
                .when().get("/ceremonies")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "LEAD")
    public void testListCeremonies() {
        given()
                .when().get("/ceremonies")
                .then()
                .statusCode(200)
                .body(containsString("CEREMONY REGISTRY"));
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "LEAD")
    public void testCreateCeremonyForm() {
        given()
                .when().get("/ceremonies/new")
                .then()
                .statusCode(200)
                .body(containsString("PROTOCOL CONFIG"))
                .body(containsString("Protocol Designation"));
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "LEAD")
    public void testCreateAndEditCeremony() throws Exception {
        userTransaction.begin();
        Team team = Team.find("name", "Engineering").firstResult();
        userTransaction.commit();

        // Create a ceremony
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("title", "Test Debrief")
                .formParam("description", "A test ceremony")
                .formParam("teamId", team.id)
                .formParam("type", "PULSE")
                .formParam("scheduleType", "DAILY")
                .formParam("isActive", "on")
                .redirects().follow(false)
                .when().post("/ceremonies")
                .then()
                .statusCode(303); // Redirect to edit page

        try {
            // Verify it was created
            given()
                    .when().get("/ceremonies")
                    .then()
                    .statusCode(200)
                    .body(containsString("Test Debrief"));
        } finally {
            // Cleanup
            userTransaction.begin();
            Ceremony.delete("title", "Test Debrief");
            userTransaction.commit();
        }
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "LEAD")
    public void testEditCeremonyPage() throws Exception {
        userTransaction.begin();
        Team team = Team.find("name", "Engineering").firstResult();
        Ceremony c = new Ceremony();
        c.team = team;
        c.type = CeremonyType.PULSE;
        c.isActive = true;
        c.title = "Edit Test Protocol";
        c.scheduleType = "WEEKLY";
        c.persist();
        userTransaction.commit();

        try {
            given()
                    .when().get("/ceremonies/" + c.id + "/edit")
                    .then()
                    .statusCode(200)
                    .body(containsString("PROTOCOL EDITOR"))
                    .body(containsString("Edit Test Protocol"))
                    .body(containsString("QUERY MATRIX"));
        } finally {
            userTransaction.begin();
            Ceremony.delete("title", "Edit Test Protocol");
            userTransaction.commit();
        }
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "LEAD")
    public void testAddAndDeleteQuestion() throws Exception {
        userTransaction.begin();
        Team team = Team.find("name", "Engineering").firstResult();
        Ceremony c = new Ceremony();
        c.team = team;
        c.type = CeremonyType.PULSE;
        c.isActive = true;
        c.title = "Question CRUD Protocol";
        c.persist();
        userTransaction.commit();

        try {
            // Add a question
            given()
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("text", "How is your morale today?")
                    .formParam("type", "SCALE")
                    .formParam("isRequired", "on")
                    .when().post("/ceremonies/" + c.id + "/questions")
                    .then()
                    .statusCode(200)
                    .body(containsString("How is your morale today?"))
                    .body(containsString("SCALE"));

            // Verify question was persisted
            userTransaction.begin();
            long count = CeremonyQuestion.count("ceremony", c);
            userTransaction.commit();
            assertEquals(1, count);

            // Get the question ID for deletion
            userTransaction.begin();
            CeremonyQuestion q = CeremonyQuestion.find("ceremony", c).firstResult();
            Long qId = q.id;
            userTransaction.commit();

            // Delete the question
            given()
                    .when().post("/ceremonies/" + c.id + "/questions/" + qId + "/delete")
                    .then()
                    .statusCode(200);

            // Verify deletion
            userTransaction.begin();
            long countAfter = CeremonyQuestion.count("ceremony", c);
            userTransaction.commit();
            assertEquals(0, countAfter);
        } finally {
            userTransaction.begin();
            CeremonyQuestion.delete("ceremony", c);
            Ceremony.delete("title", "Question CRUD Protocol");
            userTransaction.commit();
        }
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "LEAD")
    public void testQuestionReorder() throws Exception {
        userTransaction.begin();
        Team team = Team.find("name", "Engineering").firstResult();
        Ceremony c = new Ceremony();
        c.team = team;
        c.type = CeremonyType.PULSE;
        c.isActive = true;
        c.title = "Reorder Protocol";
        c.persist();

        CeremonyQuestion q1 = new CeremonyQuestion();
        q1.ceremony = c;
        q1.text = "First Question";
        q1.type = "TEXT";
        q1.sequence = 1;
        q1.isRequired = false;
        q1.persist();

        CeremonyQuestion q2 = new CeremonyQuestion();
        q2.ceremony = c;
        q2.text = "Second Question";
        q2.type = "SCALE";
        q2.sequence = 2;
        q2.isRequired = true;
        q2.persist();
        userTransaction.commit();

        try {
            // Move q2 up — should swap with q1
            given()
                    .when().post("/ceremonies/" + c.id + "/questions/" + q2.id + "/move/up")
                    .then()
                    .statusCode(200);

            // Verify the order swapped
            userTransaction.begin();
            CeremonyQuestion refreshedQ1 = CeremonyQuestion.findById(q1.id);
            CeremonyQuestion refreshedQ2 = CeremonyQuestion.findById(q2.id);
            assertEquals(2, refreshedQ1.sequence);
            assertEquals(1, refreshedQ2.sequence);
            userTransaction.commit();
        } finally {
            userTransaction.begin();
            CeremonyQuestion.delete("ceremony", c);
            Ceremony.delete("title", "Reorder Protocol");
            userTransaction.commit();
        }
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "LEAD")
    public void testDeleteCeremony() throws Exception {
        userTransaction.begin();
        Team team = Team.find("name", "Engineering").firstResult();
        Ceremony c = new Ceremony();
        c.team = team;
        c.type = CeremonyType.PULSE;
        c.isActive = false;
        c.title = "Delete Me Protocol";
        c.persist();
        Long cId = c.id;
        userTransaction.commit();

        // Delete the ceremony
        given()
                .redirects().follow(false)
                .when().post("/ceremonies/" + cId + "/delete")
                .then()
                .statusCode(303);

        // Verify it's gone
        userTransaction.begin();
        Ceremony deleted = Ceremony.findById(cId);
        userTransaction.commit();
        assertEquals(null, deleted);
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "LEAD")
    public void testToggleActiveStatus() throws Exception {
        userTransaction.begin();
        Team team = Team.find("name", "Engineering").firstResult();
        Ceremony c = new Ceremony();
        c.team = team;
        c.type = CeremonyType.PULSE;
        c.isActive = true;
        c.title = "Toggle Protocol";
        c.persist();
        userTransaction.commit();

        try {
            // Toggle — should now be inactive
            given()
                    .when().post("/ceremonies/" + c.id + "/toggle-active")
                    .then()
                    .statusCode(200)
                    .body(containsString("INACTIVE"));

            // Toggle again — should be active
            given()
                    .when().post("/ceremonies/" + c.id + "/toggle-active")
                    .then()
                    .statusCode(200)
                    .body(containsString("ACTIVE"));
        } finally {
            userTransaction.begin();
            Ceremony.delete("title", "Toggle Protocol");
            userTransaction.commit();
        }
    }
}
