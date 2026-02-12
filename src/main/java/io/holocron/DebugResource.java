package io.holocron;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;

@Path("/debug")
public class DebugResource {

    @Inject
    SecurityIdentity identity;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        if (identity.isAnonymous()) {
            return "Welcome to Holocron! (Anonymous)";
        }
        return "Welcome back, " + identity.getPrincipal().getName() + "!";
    }

    @GET
    @Path("/secure")
    @Authenticated
    @Produces(MediaType.TEXT_PLAIN)
    public String secure() {
        return "You are safely authenticated as " + identity.getPrincipal().getName();
    }
}
