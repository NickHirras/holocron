package io.holocron.ceremony;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class CeremonyResourceTest {

    @Test
    public void testCreateAndGet() {
        Ceremony ceremony = new Ceremony();
        ceremony.title = "Test Ceremony";
        ceremony.description = "A test ceremony";
        ceremony.scheduleType = "DAILY";

        given()
                .contentType(ContentType.JSON)
                .body(ceremony)
                .when()
                .post("/ceremonies")
                .then()
                .statusCode(201)
                .body("title", equalTo("Test Ceremony"))
                .body("id", notNullValue());
    }
}
