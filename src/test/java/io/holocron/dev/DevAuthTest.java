package io.holocron.dev;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
@TestProfile(DevModeProfile.class) // Use a custom profile to ensure %dev props are active?
// Actually, QuarkusTest runs in 'test' profile by default.
// We need to enable the dev-mode flags in the test profile.
public class DevAuthTest {

    @Test
    public void testDevLoginRedirect() {
        given()
                .redirects().follow(false)
                .when().get("/dev/auth/login/alice@holocron.io")
                .then()
                .statusCode(303)
                .cookie("dev-session");
    }

    @Test
    public void testSecureEndpointWithMockCookie() {
        String token = Base64.getEncoder().encodeToString("alice@holocron.io".getBytes());
        Cookie cookie = new Cookie.Builder("dev-session", token).build();

        given()
                .cookie(cookie)
                .when().get("/secure")
                .then()
                .statusCode(200)
                .body(containsString("alice@holocron.io"));
    }
}
