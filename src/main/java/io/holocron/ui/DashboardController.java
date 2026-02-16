package io.holocron.ui;

import io.holocron.ceremony.Ceremony;
import io.holocron.pulse.PulseService;
import io.holocron.team.Team;
import io.holocron.user.User;
import io.holocron.user.UserStats;
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

    @Inject
    io.holocron.report.StatsService statsService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @jakarta.transaction.Transactional
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
        // Note: The fallback user above is not persisted, so findByUser(user) below
        // will return an empty list.
        // This correctly results in a "No Team" / Drifter state for unassigned users.

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

        List<io.holocron.report.StatsService.LeaderStat> leaderStats = java.util.Collections.emptyList();
        if (user != null) {
            leaderStats = statsService.getLeaderStats(user);
            ensureStats(user);
        }

        return dashboard
                .data("user", user)
                .data("team", team)
                .data("teams", teams)
                .data("hasActivePulse", hasActivePulse)
                .data("activeCeremony", activeCeremony)
                .data("hasSubmitted", hasSubmitted)
                .data("leaderStats", leaderStats)
                .data("systemTime", java.time.LocalDateTime.now());
    }

    private void ensureStats(User user) {
        if (user.stats == null) {
            if (user.id != null) {
                UserStats stats = io.holocron.user.UserStats.findByUser(user);
                if (stats != null) {
                    user.stats = stats;
                } else {
                    stats = new io.holocron.user.UserStats();
                    stats.user = user;
                    stats.persist();
                    user.stats = stats;
                }
            } else {
                user.stats = new io.holocron.user.UserStats();
            }
        }
    }
}
