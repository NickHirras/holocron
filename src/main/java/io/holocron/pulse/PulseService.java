package io.holocron.pulse;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyAnswer;
import io.holocron.ceremony.CeremonyQuestion;
import io.holocron.ceremony.CeremonyResponse;
import io.holocron.ceremony.CeremonyType;
import io.holocron.team.Team;
import io.holocron.user.User;
import io.holocron.team.TeamMember;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PulseService {

    @jakarta.inject.Inject
    StreakService streakService;

    public Optional<Ceremony> findActivePulse(Team team) {
        return Ceremony.find("team = ?1 and type = ?2 and isActive = true", team, CeremonyType.PULSE)
                .firstResultOptional();
    }

    public boolean hasSubmitted(Ceremony ceremony, User user, LocalDate date) {
        return CeremonyResponse.count("ceremony = ?1 and user = ?2 and date = ?3", ceremony, user, date) > 0;
    }

    public java.util.List<CeremonyResponse> findResponses(Ceremony ceremony, LocalDate date) {
        return CeremonyResponse.list("ceremony = ?1 and date = ?2", io.quarkus.panache.common.Sort.by("submittedAt"),
                ceremony, date);
    }

    @Transactional
    public void createPulse(Ceremony ceremony) {
        if (!ceremony.isActive) {
            return;
        }

        List<TeamMember> members = TeamMember.find("team", ceremony.team).list();
        for (TeamMember member : members) {
            createPlaceholder(ceremony, member.user);
        }
    }

    private void createPlaceholder(Ceremony ceremony, User user) {
        LocalDate today = LocalDate.now();
        if (!hasSubmitted(ceremony, user, today)) {
            CeremonyResponse response = new CeremonyResponse();
            response.ceremony = ceremony;
            response.user = user;
            response.date = today;
            // submittedAt remains null for placeholder
            response.persist();
        }
    }

    @Transactional
    public void submitResponse(Ceremony ceremony, User user, Map<Long, String> answers, String comments) {
        LocalDate today = LocalDate.now();

        Optional<CeremonyResponse> existing = CeremonyResponse
                .find("ceremony = ?1 and user = ?2 and date = ?3", ceremony, user, today).firstResultOptional();
        CeremonyResponse response;

        if (existing.isPresent()) {
            response = existing.get();
            if (response.submittedAt != null) {
                throw new IllegalStateException("User has already submitted for today.");
            }
        } else {
            response = new CeremonyResponse();
            response.ceremony = ceremony;
            response.user = user;
            response.date = today;
            response.persist();
        }

        response.submittedAt = LocalDateTime.now();
        response.comments = comments;

        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            CeremonyQuestion question = CeremonyQuestion.findById(entry.getKey());
            if (question != null && question.ceremony.id.equals(ceremony.id)) {
                CeremonyAnswer answer = new CeremonyAnswer();
                answer.response = response;
                answer.question = question;
                answer.answerValue = entry.getValue();
                answer.persist();
            }
        }

        streakService.incrementStreak(user);
        streakService.incrementXp(user, 10, "Pulse Submission");
    }
}
