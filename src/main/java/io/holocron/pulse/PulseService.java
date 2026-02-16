package io.holocron.pulse;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyAnswer;
import io.holocron.ceremony.CeremonyQuestion;
import io.holocron.ceremony.CeremonyResponse;
import io.holocron.ceremony.CeremonyType;
import io.holocron.team.Team;
import io.holocron.user.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PulseService {

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
    public void submitResponse(Ceremony ceremony, User user, Map<Long, String> answers, String comments) {
        LocalDate today = LocalDate.now();
        if (hasSubmitted(ceremony, user, today)) {
            throw new IllegalStateException("User has already submitted for today.");
        }

        CeremonyResponse response = new CeremonyResponse();
        response.ceremony = ceremony;
        response.user = user;
        response.date = today;
        response.submittedAt = LocalDateTime.now();
        response.comments = comments;
        response.persist();

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
    }
}
