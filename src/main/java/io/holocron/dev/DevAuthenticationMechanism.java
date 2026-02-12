package io.holocron.dev;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import jakarta.ws.rs.core.Cookie;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Priority(1000) // Ensure this runs before other mechanisms if active
public class DevAuthenticationMechanism implements HttpAuthenticationMechanism {

    @ConfigProperty(name = "holocron.auth.dev-mode", defaultValue = "false")
    boolean devMode;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        if (!devMode) {
            return Uni.createFrom().optional(Optional.empty());
        }

        io.vertx.core.http.Cookie cookie = context.request().getCookie("dev-session");
        if (cookie != null) {
            String email = new String(Base64.getDecoder().decode(cookie.getValue()));

            return Uni.createFrom().item(QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal(email))
                    .addAttribute("email", email)
                    .addRole("user")
                    .build());
        }

        return Uni.createFrom().optional(Optional.empty());
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        if (!devMode) {
            return Uni.createFrom().optional(Optional.empty());
        }
        // In dev mode, if not authenticated, we just return 401 and let the frontend
        // handle it,
        // or we could redirect to a login page if dealing with full page loads.
        return Uni.createFrom().item(new ChallengeData(401, null, null));
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of();
    }

    @Override
    public Uni<Boolean> sendChallenge(RoutingContext context) {
        if (!devMode) {
            return Uni.createFrom().item(false);
        }
        // Simple 401 for now
        context.response().setStatusCode(401).end();
        return Uni.createFrom().item(true);
    }
}
