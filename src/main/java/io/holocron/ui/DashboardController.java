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
import java.time.LocalDate;
import java.util.Optional;

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
    public TemplateInstance index() {
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

        // 2. Fetch Team (Defaulting to "Engineering" or first found for now)
        Team team = Team.find("name", "Engineering").firstResult();
        if (team == null) {
            team = Team.findAll().firstResult();
        }

        if (team == null) {
            team = new Team();
            team.name = "Unknown Sector";
            team.id = 0L;
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
                .data("hasActivePulse", hasActivePulse)
                .data("activeCeremony", activeCeremony)
                .data("hasSubmitted", hasSubmitted)
                .data("systemTime", java.time.LocalDateTime.now());
    }
}
