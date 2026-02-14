package io.holocron.ui;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class DashboardControllerTest {

    @Test
    public void testDashboardRedirectsAnonymous() {
        given()
                .when().get("/dashboard")
                .then()
                .statusCode(401); // Standard for API/Quarkus default, though UI might want 302
    }

    @Test
    @TestSecurity(user = "alice@holocron.io", roles = "user")
    public void testDashboardLoading() {
        given()
                .when().get("/dashboard")
                .then()
                .statusCode(200)
                .body(containsString("COMMAND DECK"))
                .body(containsString("alice@holocron.io"));
    }
}
