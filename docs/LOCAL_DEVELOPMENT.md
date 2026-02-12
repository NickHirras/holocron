# Local Development & Authentication

Holocron is designed to be "Jedi-Grade": powerful but with zero friction. We shouldn't need a full Keycloak instance or an internet connection to build the future of ceremonies.

## ⚡ The "Offline Dev" Strategy

In Production, we use OIDC (OpenID Connect) to talk to GitHub/Google.
In Development, we use a **Custom Quarkus Security Mechanism** that bypasses OIDC entirely.

### Architecture

1.  **Production (`prod`)**: `quarkus-oidc` is active. Users are redirected to the Identity Provider.
2.  **Development (`dev`)**: `quarkus-oidc` is **disabled**. A custom `HttpAuthenticationMechanism` intercepts requests and sets a "logged in" state based on a simple cookie.

This means you can "login" as any user persona instanty, even while on an airplane.

---

## 🛠️ Implementation Guide

Since we don't have the source code scaffolding yet, here is the blueprint for how to implement this when we write the code.

### 1. `application.properties`

Disable OIDC tenant by default in dev profile:

```properties
# %prod profile (Default)
quarkus.oidc.auth-server-url=https://...
quarkus.oidc.client-id=...

# %dev profile
%dev.quarkus.oidc.enabled=false
%dev.holocron.auth.dev-mode=true
```

### 2. The Dev Login Page (`DevResource.java`)

A simple endpoint that is only active in `dev` profile. It sets a cookie to simulate a session.

```java
@Path("/dev/auth")
@ApplicationScoped
public class DevResource {

    @ConfigProperty(name = "holocron.auth.dev-mode", defaultValue = "false")
    boolean devMode;

    @GET
    @Path("/login/{email}")
    public Response mockLogin(@PathParam("email") String email) {
        if (!devMode) return Response.status(404).build();

        // Create a mock cookie "dev-session"
        NewCookie cookie = new NewCookie.Builder("dev-session")
                .value(Base64.getEncoder().encodeToString(email.getBytes()))
                .path("/")
                .build();

        return Response.seeOther(URI.create("/"))
                .cookie(cookie)
                .build();
    }
}
```

### 3. The Security Mechanism (`DevAuthenticationMechanism.java`)

This intercepts every request. If we are in dev mode and see the cookie, we trust it.

```java
@ApplicationScoped
@Priority(1000) // Run before other auth mechanisms
public class DevAuthenticationMechanism implements HttpAuthenticationMechanism {

    @ConfigProperty(name = "holocron.auth.dev-mode", defaultValue = "false")
    boolean devMode;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        if (!devMode) {
            return Uni.createFrom().optional(Optional.empty());
        }

        // Check for our mock cookie
        Cookie cookie = context.request().getCookie("dev-session");
        if (cookie != null) {
            String email = new String(Base64.getDecoder().decode(cookie.getValue()));
            
            // Build a "QuarkusSecurityIdentity"
            return Uni.createFrom().item(QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal(email))
                    .addAttribute("email", email)
                    .addRole("user") // Default role
                    .build());
        }

        return Uni.createFrom().optional(Optional.empty());
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        // If not authenticated, redirect to a simple dev login list
        return Uni.createFrom().item(new ChallengeData(401, null, null));
    }
}
```

## NOTE

The code samples above are for illustration purposes only. They are not guaranteed to be correct or complete. Do not use them as a reference for how to build this functionality.

## 🚀 How to use it

Once implemented:

1.  Run `quarkus dev`.
2.  Go to `localhost:8080`.
3.  You will be redirected (or can navigate) to a dev-only page listing "Login as Alice", "Login as Bob".
4.  Clicking them hits `/dev/auth/login/alice@holocron.io`, sets the cookie, and you are in.
