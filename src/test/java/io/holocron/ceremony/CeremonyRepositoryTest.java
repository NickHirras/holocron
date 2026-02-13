package io.holocron.ceremony;

import io.holocron.team.Team;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class CeremonyRepositoryTest {

    @Inject
    CeremonyRepository ceremonyRepository;

    @Inject
    CeremonyQuestionRepository ceremonyQuestionRepository;

    @Test
    @TestTransaction
    public void testCeremonyCreationAndQuestions() {
        // Create a Team
        Team team = new Team();
        team.name = "Engineering";
        team.persist();

        // Create a Ceremony
        Ceremony ceremony = new Ceremony();
        ceremony.title = "Daily Standup";
        ceremony.team = team;
        ceremony.scheduleType = "DAILY";
        ceremony.isActive = true;
        ceremonyRepository.persist(ceremony);

        // Create Questions
        CeremonyQuestion q1 = new CeremonyQuestion();
        q1.ceremony = ceremony;
        q1.text = "What did you do yesterday?";
        q1.type = "TEXT";
        q1.sequence = 1;
        ceremonyQuestionRepository.persist(q1);

        CeremonyQuestion q2 = new CeremonyQuestion();
        q2.ceremony = ceremony;
        q2.text = "Blockers?";
        q2.type = "BOOLEAN";
        q2.sequence = 2;
        ceremonyQuestionRepository.persist(q2);

        // Verify
        List<Ceremony> teamCeremonies = ceremonyRepository.findByTeam(team);
        assertEquals(1, teamCeremonies.size());
        assertEquals("Daily Standup", teamCeremonies.get(0).title);

        List<CeremonyQuestion> questions = ceremonyQuestionRepository.findByCeremony(ceremony);
        assertEquals(2, questions.size());
        assertEquals("What did you do yesterday?", questions.get(0).text);
        assertEquals("Blockers?", questions.get(1).text);
    }
}
