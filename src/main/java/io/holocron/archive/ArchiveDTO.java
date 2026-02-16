package io.holocron.archive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ArchiveDTO {

    public Long ceremonyId;
    public String ceremonyTitle;
    public LocalDate periodStart;
    public LocalDate periodEnd;
    public LocalDateTime generatedAt;
    public List<ResponseDTO> responses;

    public static class ResponseDTO {
        public Long userId;
        public String userName; // Snapshot of user name at time of archive
        public String userAvatar; // Snapshot of avatar if needed, or just rely on ID for now
        public LocalDateTime submittedAt;
        public String comments;
        public List<AnswerDTO> answers;
    }

    public static class AnswerDTO {
        public Long questionId;
        public String questionText; // Snapshot of question text
        public String answerValue;
    }
}
