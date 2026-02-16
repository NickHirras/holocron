package io.holocron.report;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyResponse;
import io.holocron.pulse.PulseService;
import io.holocron.team.Team;
import io.holocron.team.TeamMember;
import io.holocron.user.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class StatsService {

    @Inject
    PulseService pulseService;

    public List<LeaderStat> getLeaderStats(User user) {
        List<LeaderStat> stats = new ArrayList<>();
        List<TeamMember> memberships = TeamMember.findByUser(user);

        for (TeamMember membership : memberships) {
            Team team = membership.team;
            Optional<Ceremony> activePulse = pulseService.findActivePulse(team);

            LeaderStat stat = new LeaderStat();
            stat.teamName = team.name;
            stat.teamId = team.id;
            stat.hasActivePulse = activePulse.isPresent();

            if (activePulse.isPresent()) {
                stat.hasSubmitted = pulseService.hasSubmitted(activePulse.get(), user, LocalDate.now());
            } else {
                stat.hasSubmitted = false;
            }
            stats.add(stat);
        }
        return stats;
    }

    public DailyRollup getDailyRollup(Team team, LocalDate date) {
        DailyRollup rollup = new DailyRollup();
        rollup.team = team;
        rollup.date = date;

        Optional<Ceremony> pulse = pulseService.findActivePulse(team);
        // Note: Logic here is simplified. We might want to find *any* pulse that was
        // active on that date,
        // but for now we look for the currently active one or just responses if we had
        // historical pulse knowledge.
        // Given current simple model, we'll try to find responses linked to the team's
        // pulse ceremony type.
        // Actually, getting responses for a specific date is safer even if the ceremony
        // isn't "active" right now but exists.

        // Lets find the Pulse ceremony for this team.
        Ceremony ceremony = (Ceremony) Ceremony
                .find("team = ?1 and type = ?2", team, io.holocron.ceremony.CeremonyType.PULSE).firstResult();

        if (ceremony != null) {
            rollup.responses = pulseService.findResponses(ceremony, date);
            rollup.missingMembers = new ArrayList<>();

            List<TeamMember> members = TeamMember.findByTeam(team);
            List<Long> submitterIds = rollup.responses.stream().map(r -> r.user.id).toList();

            for (TeamMember member : members) {
                if (!submitterIds.contains(member.user.id)) {
                    rollup.missingMembers.add(member.user);
                }
            }
        } else {
            rollup.responses = new ArrayList<>();
            rollup.missingMembers = new ArrayList<>();
        }

        return rollup;
    }

    // DTOs
    public static class LeaderStat {
        public Long teamId;
        public String teamName;
        public boolean hasActivePulse;
        public boolean hasSubmitted;
    }

    public static class DailyRollup {
        public Team team;
        public LocalDate date;
        public List<CeremonyResponse> responses;
        public List<User> missingMembers;
    }
}
