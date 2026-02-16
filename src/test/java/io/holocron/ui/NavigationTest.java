package io.holocron.ui;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@QuarkusTest
public class NavigationTest {

    @Test
    @TestSecurity(user = "test@holocron.io", roles = "Operative")
    public void testSidePanelLoads() {
        given()
                .when().get("/components/side-panel")
                .then()
                .statusCode(200)
                .body(containsString("COMMAND DECK"))
                .body(containsString("OPERATIVE PROFILE"))
                .body(containsString("TERMINATE SESSION"));
    }

    @Test
    @TestSecurity(user = "test@holocron.io", roles = "Operative")
    public void testDashboardHtmxPartial() {
        // Request with HX-Request header should return only the fragment
        given()
                .header("HX-Request", "true")
                .when().get("/dashboard")
                .then()
                .statusCode(200)
                .body(containsString("COMMAND DECK"))
                // Should NOT contain the base layout elements typically
                // But Qute renders fragments by just returning the content.
                // A good check is that it doesn't contain the <html> or <body> tags if using
                // fragment
                // But wait, getFragment just renders the block.
                // base.html stuff (like <head> or padding) should be missing.
                .body(not(containsString("<html lang=\"en\">")))
                .body(not(containsString("<!DOCTYPE html>")));
    }

    @Test
    @TestSecurity(user = "test@holocron.io", roles = "Operative")
    public void testProfileHtmxPartial() {
        given()
                .header("HX-Request", "true")
                .when().get("/profile")
                .then()
                .statusCode(200)
                .body(containsString("OPERATIVE PROFILE"))
                .body(not(containsString("<html lang=\"en\">")));
    }
}
