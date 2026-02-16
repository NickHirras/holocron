package io.holocron.ui;

import io.holocron.team.Team;
import io.holocron.user.User;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import io.holocron.team.TeamMember;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Response;
import java.net.URI;
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
    public TemplateInstance index(@HeaderParam("HX-Request") boolean hxRequest) {
        // 1. Identify User
        String email = identity.getPrincipal().getName();
        User user = getOrCreateUser(email);

        // 2. Fetch Memberships (My Teams)
        List<TeamMember> memberships = TeamMember.findByUser(user);

        // 3. Fetch Available Teams (Teams user is NOT in)
        List<Team> allTeams = Team.listAll();
        List<Long> memberTeamIds = memberships.stream().map(m -> m.team.id).toList();
        List<Team> availableTeams = allTeams.stream()
                .filter(t -> !memberTeamIds.contains(t.id))
                .toList();

        if (hxRequest) {
            return profile.getFragment("profile_content")
                    .data("user", user)
                    .data("memberships", memberships)
                    .data("availableTeams", availableTeams);
        }

        return profile
                .data("user", user)
                .data("memberships", memberships)
                .data("availableTeams", availableTeams);
    }

    @POST
    @Path("/join")
    @Transactional
    @io.holocron.audit.Audited(action = "JOIN_TEAM")
    public Response joinTeam(@FormParam("teamId") Long teamId) {
        String email = identity.getPrincipal().getName();
        User user = getOrCreateUser(email);
        Team team = Team.findById(teamId);

        if (user != null && team != null) {
            // Check if already a member
            if (TeamMember.find("user = ?1 and team = ?2", user, team).count() == 0) {
                TeamMember membership = new TeamMember();
                membership.user = user;
                membership.team = team;
                membership.role = "Operative"; // Default role
                membership.persist();
            }
        }
        return Response.seeOther(URI.create("/profile")).build();
    }

    @POST
    @Path("/update")
    @Transactional
    @io.holocron.audit.Audited(action = "UPDATE_PROFILE")
    public Response updateProfile(@FormParam("name") String name) {
        String email = identity.getPrincipal().getName();
        User user = getOrCreateUser(email);

        if (user != null) {
            user.name = name;
            user.persist(); // Ensure updates are saved
        }
        return Response.seeOther(URI.create("/profile")).build();
    }

    @Transactional
    User getOrCreateUser(String email) {
        User user = User.findByEmail(email);
        if (user == null) {
            user = new User();
            user.email = email;
            user.name = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
            user.role = "Operative";
            user.persist();
        }
        return user;
    }
}
