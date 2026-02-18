package io.holocron.ceremony;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class CeremonyQuestionResourceTest {

        @Test
        public void testReorder() {
                // Create Ceremony via API
                Ceremony ceremony = new Ceremony();
                ceremony.title = "Reorder Test";

                Integer ceremonyId = given()
                                .contentType(ContentType.JSON)
                                .body(ceremony)
                                .post("/ceremonies")
                                .then()
                                .statusCode(201)
                                .extract().path("id");

                // Create Questions
                Integer q1 = createQuestion(ceremonyId.longValue(), "Q1");
                Integer q2 = createQuestion(ceremonyId.longValue(), "Q2");
                Integer q3 = createQuestion(ceremonyId.longValue(), "Q3");

                // Reorder: 3, 1, 2
                given()
                                .contentType(ContentType.JSON)
                                .body(List.of(q3, q1, q2))
                                .post("/ceremonies/" + ceremonyId + "/questions/reorder")
                                .then()
                                .statusCode(200);

                // Verify
                given()
                                .get("/ceremonies/" + ceremonyId + "/questions")
                                .then()
                                .statusCode(200)
                                .body("id", contains(q3, q1, q2));
        }

        @Test
        public void testMove() {
                Ceremony ceremony = new Ceremony();
                ceremony.title = "Move Test";

                Integer ceremonyId = given()
                                .contentType(ContentType.JSON)
                                .body(ceremony)
                                .post("/ceremonies")
                                .then()
                                .statusCode(201)
                                .extract().path("id");

                Integer q1 = createQuestion(ceremonyId.longValue(), "Q1");
                Integer q2 = createQuestion(ceremonyId.longValue(), "Q2");

                // Move Q1 DOWN (should be after Q2)
                given()
                                .contentType(ContentType.JSON)
                                .post("/ceremonies/" + ceremonyId + "/questions/" + q1 + "/move?dir=DOWN")
                                .then()
                                .statusCode(200);

                // Verify
                given()
                                .get("/ceremonies/" + ceremonyId + "/questions")
                                .then()
                                .statusCode(200)
                                .body("id", contains(q2, q1));
        }

        private Integer createQuestion(Long ceremonyId, String text) {
                CeremonyQuestion q = new CeremonyQuestion();
                q.text = text;
                q.type = "TEXT";
                return given()
                                .contentType(ContentType.JSON)
                                .body(q)
                                .post("/ceremonies/" + ceremonyId + "/questions")
                                .then()
                                .statusCode(201)
                                .extract().path("id");
        }
}
