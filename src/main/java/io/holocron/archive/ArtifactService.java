package io.holocron.archive;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.holocron.audit.AuditEntry;
import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyAnswer;
import io.holocron.ceremony.CeremonyResponse;
import io.holocron.pulse.PulseService;
import io.holocron.team.Team;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ArtifactService {

    @Inject
    PulseService pulseService;

    @Inject
    ObjectMapper objectMapper;

    @Transactional
    public Artifact generateArtifact(Ceremony ceremony, LocalDate periodStart, LocalDate periodEnd) {
        // 1. Check if artifact already exists for this period
        long count = Artifact.count("ceremony = ?1 and periodStart = ?2 and periodEnd = ?3", ceremony, periodStart,
                periodEnd);
        if (count > 0) {
            throw new IllegalStateException("Artifact already exists for this period.");
        }

        // 2. Aggregate Data
        // For now, assuming standard Pulse is daily and we capture responses for the
        // periodEnd date.
        List<CeremonyResponse> responses = pulseService.findResponses(ceremony, periodEnd);

        ArchiveDTO archiveDTO = new ArchiveDTO();
        archiveDTO.ceremonyId = ceremony.id;
        archiveDTO.ceremonyTitle = ceremony.title;
        archiveDTO.periodStart = periodStart;
        archiveDTO.periodEnd = periodEnd;
        archiveDTO.generatedAt = LocalDateTime.now();

        archiveDTO.responses = responses.stream().map(response -> {
            ArchiveDTO.ResponseDTO responseDTO = new ArchiveDTO.ResponseDTO();
            responseDTO.userId = response.user.id;
            responseDTO.userName = response.user.name;
            responseDTO.submittedAt = response.submittedAt;
            responseDTO.comments = response.comments;

            // Fetch answers for this response
            List<CeremonyAnswer> answers = CeremonyAnswer.list("response", response);
            responseDTO.answers = answers.stream().map(answer -> {
                ArchiveDTO.AnswerDTO answerDTO = new ArchiveDTO.AnswerDTO();
                answerDTO.questionId = answer.question.id;
                answerDTO.questionText = answer.question.text;
                answerDTO.answerValue = answer.answerValue;
                return answerDTO;
            }).collect(Collectors.toList());

            return responseDTO;
        }).collect(Collectors.toList());

        // 3. Serialize Data
        String summaryJson;
        try {
            summaryJson = objectMapper.writeValueAsString(archiveDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize artifact data", e);
        }

        // 4. Create Artifact
        Artifact artifact = new Artifact();
        artifact.team = ceremony.team;
        artifact.ceremony = ceremony;
        artifact.periodStart = periodStart;
        artifact.periodEnd = periodEnd;
        artifact.summaryJson = summaryJson;
        artifact.createdAt = LocalDateTime.now();
        artifact.persist();

        // 5. Log Audit
        AuditEntry.log("SYSTEM", "ARTIFACT_GENERATION",
                "Generated artifact for " + ceremony.title + " [" + periodStart + " - " + periodEnd + "]");

        return artifact;
    }

    public List<Artifact> findRecentArtifacts(Team team) {
        if (team == null)
            return java.util.Collections.emptyList();
        return Artifact.find("team = ?1 order by periodEnd desc", team).page(0, 10).list();
    }
}
