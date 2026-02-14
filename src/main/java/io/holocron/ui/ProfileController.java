package io.holocron.ui;

import io.holocron.team.Team;
import io.holocron.user.User;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/profile")
@Authenticated
public class ProfileController {

    @Inject
    Template profile;

    @Inject
    SecurityIdentity identity;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        // 1. Identify User
        String email = identity.getPrincipal().getName();
        User user = User.findByEmail(email);

        // Fallback for dev/mocking
        if (user == null) {
            user = new User();
            user.email = email;
            user.name = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
            user.role = "Operative";
        }

        // 2. Fetch Teams (For now, just listing all available teams as "Assignment
        // Options" or "Current Assignments")
        // In a real scenario, we'd filter by user.teams
        List<Team> teams = Team.listAll();

        return profile
                .data("user", user)
                .data("teams", teams);
    }
}
