package io.holocron.ui;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class DashboardControllerTest {

    @jakarta.inject.Inject
    jakarta.transaction.UserTransaction userTransaction;

    @org.junit.jupiter.api.BeforeEach
    public void setup() throws Exception {
        userTransaction.begin();
        io.holocron.user.User user = io.holocron.user.User.findByEmail("alice@holocron.io");
        if (user == null) {
            user = new io.holocron.user.User();
            user.email = "alice@holocron.io";
            user.name = "Alice";
            user.role = "user";
            user.persist();
        }
        userTransaction.commit();
    }

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
                .body(containsString("alice@holocron.io"))
                // Check for Status Card Component
                .body(containsString("// SYSTEM STATUS")) // Assuming default state for 'user' role
                // Check for Leader Stats Component
                .body(containsString("// LEADER STATS"));
    }
}
