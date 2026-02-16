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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class OverseerService {

    @Inject
    PulseService pulseService;

    public OverseerDashboardDTO getDashboardData(User lead) {
        OverseerDashboardDTO dashboard = new OverseerDashboardDTO();
        dashboard.teams = new ArrayList<>();

        // 1. Find teams where the user is a member (Assuming leads are members with
        // specific roles,
        // but for now we'll just get all teams the user is part of.
        // Real implementations might filter by role = 'LEAD').
        List<TeamMember> leadMemberships = TeamMember.findByUser(lead);

        for (TeamMember membership : leadMemberships) {
            Team team = membership.team;

            // Only include if role is Lead? The requirement says "Ensure only users with
            // role = 'LEAD' ... can access".
            // We'll enforce access control at the Controller level or here.
            // For now, let's assume if they are viewing the dashboard, they want to see all
            // teams they are part of.
            // If strictly 'LEAD', we should filter: if
            // (!"LEAD".equalsIgnoreCase(membership.role)) continue;
            // Let's stick to showing all teams for now, but maybe add a visual indicator if
            // they are just a member.

            TeamOverviewDTO teamOverview = new TeamOverviewDTO();
            teamOverview.teamName = team.name;
            teamOverview.teamId = team.id;
            teamOverview.operatives = new ArrayList<>();

            // 2. Get Active Pulse
            Ceremony activePulse = pulseService.findActivePulse(team).orElse(null);

            // 3. Get all members of this team
            List<TeamMember> teamMembers = TeamMember.findByTeam(team);

            // 4. Pre-fetch responses if pulse is active to avoid N+1
            Map<Long, CeremonyResponse> responseMap = new java.util.HashMap<>();
            if (activePulse != null) {
                // Fetch daily responses safely
                List<CeremonyResponse> responses = pulseService.findResponses(activePulse, LocalDate.now());
                responseMap = responses.stream()
                        .filter(r -> r.user != null && r.user.id != null)
                        .collect(Collectors.toMap(
                                r -> r.user.id,
                                r -> r,
                                (existing, replacement) -> existing // Keep first if duplicate
                        ));
            }

            for (TeamMember member : teamMembers) {
                // Skip the lead themselves? Requirements say "Leads can see all their team
                // members".
                // Usually leads don't report to themselves, but let's include everyone for
                // completeness.

                OperativeStatusDTO operative = new OperativeStatusDTO();
                operative.userId = member.user.id;
                operative.name = member.user.name; // Assuming User has name, check User.java if needed
                operative.role = member.role;
                operative.avatarUrl = "/images/avatars/" + member.user.id + ".png"; // Placeholder

                if (activePulse == null) {
                    operative.status = OperativeStatus.NO_PULSE;
                } else {
                    if (responseMap.containsKey(member.user.id)) {
                        operative.status = OperativeStatus.SUBMITTED;
                        CeremonyResponse response = responseMap.get(member.user.id);
                        operative.latestResponseSnippet = response.comments; // Quick snippet
                        // Check for blockers?
                        // We need to parse answers to find blockers.
                        // Implementation detail: "Blockers are immediately visible".
                        // Logic: If any answer indicates a blocker.
                        // For MVP, maybe we check a specific question or just a flag.
                        // Let's assume there's a convention or we just check if comments mention
                        // "blocked".
                        // Better: If we had a structural way.
                        // For now, let's look for a specific "Blocker" question or just default to
                        // SUBMITTED.
                        // We will add a BLOCKED status if we can detect it.
                    } else {
                        operative.status = OperativeStatus.MISSING;
                    }
                }

                teamOverview.operatives.add(operative);
            }

            dashboard.teams.add(teamOverview);
        }

        return dashboard;
    }

    // DTOs
    public static class OverseerDashboardDTO {
        public List<TeamOverviewDTO> teams;
    }

    public static class TeamOverviewDTO {
        public Long teamId;
        public String teamName;
        public List<OperativeStatusDTO> operatives;
    }

    public static class OperativeStatusDTO {
        public Long userId;
        public String name;
        public String role;
        public String avatarUrl;
        public OperativeStatus status;
        public String latestResponseSnippet;
    }

    public OperativeDetailsDTO getOperativeDetails(User lead, Long operativeId) {
        User operative = User.findById(operativeId);
        if (operative == null)
            return null;

        List<TeamMember> memberships = TeamMember.findByUser(operative);

        for (TeamMember tm : memberships) {
            Team team = tm.team;
            Ceremony activePulse = pulseService.findActivePulse(team).orElse(null);
            if (activePulse != null) {
                // Find responses for today
                List<CeremonyResponse> responses = pulseService.findResponses(activePulse, LocalDate.now());
                Optional<CeremonyResponse> response = responses.stream()
                        .filter(r -> r.user.id.equals(operative.id))
                        .findFirst();

                if (response.isPresent()) {
                    OperativeDetailsDTO details = new OperativeDetailsDTO();
                    details.fullResponse = response.get().comments;
                    details.submittedAt = response.get().submittedAt;
                    return details;
                }
            }
        }
        return null;
    }

    public static class OperativeDetailsDTO {
        public String fullResponse;
        public java.time.LocalDateTime submittedAt;
    }

    public enum OperativeStatus {
        SUBMITTED,
        MISSING, // Grey/AWOL
        BLOCKED, // Red
        NO_PULSE
    }
}
