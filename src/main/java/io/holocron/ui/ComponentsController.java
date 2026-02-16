package io.holocron.ui;

import io.holocron.team.Team;
import io.holocron.team.TeamMember;
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
import java.util.Collections;
import java.util.List;

@Path("/components")
@Authenticated
public class ComponentsController {

    @Inject
    @io.quarkus.qute.Location("components/side_panel")
    Template side_panel;

    @Inject
    SecurityIdentity identity;

    @GET
    @Path("/side-panel")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getSidePanel() {
        String email = identity.getPrincipal().getName();
        User user = User.findByEmail(email);

        List<Team> teams = Collections.emptyList();
        if (user != null) {
            List<TeamMember> memberships = TeamMember.findByUser(user);
            if (memberships != null) {
                teams = memberships.stream().map(m -> m.team).toList();
            }
        } else {
            // Mock user for dev if needed, logic borrowed from DashboardController
            // But strictly for nav, if user is null we probably just show limited options
        }

        return side_panel.data("teams", teams);
    }
}
