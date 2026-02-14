package io.holocron.ui;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class ProfileControllerTest {

    @Test
    @TestSecurity(user = "test@example.com", roles = "Operative")
    public void testProfilePage() {
        given()
                .when().get("/profile")
                .then()
                .statusCode(200)
                .body(containsString("test@example.com"));
    }
}
