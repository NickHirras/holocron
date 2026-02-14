package io.holocron.dev;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.inject.Inject;

import java.net.URI;
import java.util.Base64;

@Path("/dev/auth")
@ApplicationScoped
public class DevAuthResource {

    @ConfigProperty(name = "holocron.auth.dev-mode", defaultValue = "false")
    boolean devMode;

    @Inject
    @Location("dev/login.html")
    Template login;

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance loginPage() {
        if (!devMode) {
            // In production, this should not be accessible
            throw new NotFoundException();
        }
        return login.data("users", io.holocron.user.User.listAll());
    }

    @GET
    @Path("/login/{email}")
    public Response mockLogin(@PathParam("email") String email) {
        if (!devMode)
            return Response.status(404).build();

        NewCookie cookie = new NewCookie.Builder("dev-session")
                .value(Base64.getEncoder().encodeToString(email.getBytes()))
                .path("/")
                .maxAge(86400) // 1 day
                .build();

        return Response.seeOther(URI.create("/"))
                .cookie(cookie)
                .build();
    }

    @GET
    @Path("/logout")
    public Response mockLogout() {
        if (!devMode)
            return Response.status(404).build();

        NewCookie cookie = new NewCookie.Builder("dev-session")
                .value("")
                .path("/")
                .maxAge(0)
                .build();

        return Response.seeOther(URI.create("/"))
                .cookie(cookie)
                .build();
    }
}
