package io.holocron.ui;

import io.holocron.ceremony.Ceremony;
import io.holocron.pulse.PulseService;
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
import jakarta.ws.rs.QueryParam;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import io.holocron.team.TeamMember;

@Path("/dashboard")
@Authenticated
public class DashboardController {

    @Inject
    Template dashboard;

    @Inject
    SecurityIdentity identity;

    @Inject
    PulseService pulseService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index(@QueryParam("teamId") Long teamId) {
        // 1. Identify User
        String email = identity.getPrincipal().getName();
        User user = User.findByEmail(email);

        // Fallback for dev/mocking if user not in DB but authenticated (shouldn't
        // happen in prod)
        if (user == null) {
            user = new User();
            user.email = email;
            user.name = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
            user.role = "Operative";
        }

        // 2. Fetch Teams & Determine Active Sector
        List<TeamMember> memberships = TeamMember.findByUser(user);
        List<Team> teams = memberships.stream().map(m -> m.team).toList();

        Team team = null;
        if (!teams.isEmpty()) {
            if (teamId != null) {
                team = teams.stream()
                        .filter(t -> t.id.equals(teamId))
                        .findFirst()
                        .orElse(null);
            } else {
                team = teams.get(0);
            }
        }

        // Handle "No Team" / Drifter state
        if (team == null) {
            // We can handle this in the template, checking if team is null
        }

        // 3. Check for Active Pulse
        boolean hasActivePulse = false;
        boolean hasSubmitted = false;
        Ceremony activeCeremony = null;

        if (team != null) {
            Optional<Ceremony> pulse = pulseService.findActivePulse(team);
            if (pulse.isPresent()) {
                hasActivePulse = true;
                activeCeremony = pulse.get();
                if (user != null) {
                    hasSubmitted = pulseService.hasSubmitted(activeCeremony, user, LocalDate.now());
                }
            }
        }

        return dashboard
                .data("user", user)
                .data("team", team)
                .data("teams", teams)
                .data("hasActivePulse", hasActivePulse)
                .data("activeCeremony", activeCeremony)
                .data("hasSubmitted", hasSubmitted)
                .data("systemTime", java.time.LocalDateTime.now());
    }
}
