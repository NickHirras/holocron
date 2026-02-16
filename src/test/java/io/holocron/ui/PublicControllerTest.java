package io.holocron.ui;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class PublicControllerTest {

    @Test
    public void testLandingPage() {
        given()
                .when().get("/")
                .then()
                .statusCode(200)
                .body(containsString("Holocron"), containsString("Initialize Protocol")); // Check for title and button
    }

}
