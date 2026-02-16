# Holocron Testing Patterns

## 🚀 Core Philosophy: The Journey is the Unit
We prioritize **Integration-level testing** using `@QuarkusTest` and `RestAssured`. We don't just test if a method returns a value; we test if the server returns the correct **Holographic HTML fragment** that HTMX expects.


## 1. Integration Testing (The "Sweet Spot")
Most tests should reside in `src/test/java/io/holocron/ui/`.

### Pattern: Authenticated UI Checks
Always use `@TestSecurity` to simulate different operative clearance levels.
```java
@Test
@TestSecurity(user = "vader@empire.gov", roles = "admin")
public void testAdminAccessToCommanderView() {
    given()
        .when().get("/dashboard")
        .then()
        .statusCode(200)
        .body(containsString("COMMAND DECK")) // Verify persona-specific UI
        .body(containsString("vader@empire.gov"));
}