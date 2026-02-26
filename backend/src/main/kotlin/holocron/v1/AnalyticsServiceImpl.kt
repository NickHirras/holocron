package holocron.v1

import holocron.v1.repository.CeremonyResponseRepository
import holocron.v1.repository.CeremonyTemplateRepository
import holocron.v1.repository.TeamRepository
import java.time.Instant

class AnalyticsServiceImpl(
    private val templateRepository: CeremonyTemplateRepository,
    private val responseRepository: CeremonyResponseRepository,
    private val teamRepository: TeamRepository
) : AnalyticsServiceGrpcKt.AnalyticsServiceCoroutineImplBase() {

    override suspend fun getTeamHealth(request: GetTeamHealthRequest): GetTeamHealthResponse {
        val teamId = request.teamId
        
        // 1. Get Team Size
        val memberships = teamRepository.getTeamMemberships(teamId)
        val teamSize = memberships.size.coerceAtLeast(1)

        // 2. Get Templates for the Team
        val templates = templateRepository.findByTeamId(teamId)
        
        // 3. Collect responses for each template within the date range
        val allResponses = mutableListOf<CeremonyResponse>()
        templates.forEach { template ->
            val responses = responseRepository.findByTemplateId(template.id, request.startTime, request.endTime)
            allResponses.addAll(responses)
        }

        // Calculate Metrics
        var totalSentimentSum = 0f
        var sentimentCount = 0
        var blockerCount = 0

        for (response in allResponses) {
            for ((_, answer) in response.answersMap) {
                when (answer.kindCase) {
                    Answer.KindCase.SCALE_ANSWER -> {
                        totalSentimentSum += answer.scaleAnswer.value
                        sentimentCount++
                    }
                    Answer.KindCase.TEXT_ANSWER -> {
                        if (answer.textAnswer.value.contains("blocker", ignoreCase = true) ||
                            answer.textAnswer.value.contains("stuck", ignoreCase = true)) {
                            blockerCount++
                        }
                    }
                    Answer.KindCase.CHOICE_ANSWER -> {
                        for (value in answer.choiceAnswer.valuesList) {
                            if (value.contains("blocker", ignoreCase = true) ||
                                value.contains("stuck", ignoreCase = true)) {
                                blockerCount++
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        val participationRate = if (templates.isEmpty()) {
            0f
        } else {
            // approximation: total responses / (team members * number of templates)
            (allResponses.size.toFloat() / (teamSize * templates.size).toFloat()) * 100f
        }

        val sentimentTrend = if (sentimentCount > 0) totalSentimentSum / sentimentCount else 0f

        val now = com.google.protobuf.Timestamp.newBuilder().setSeconds(Instant.now().epochSecond).build()

        val metrics = listOf(
            TeamMetric.newBuilder().setMetricName("Participation Rate").setValue(participationRate).setTimestamp(now).build(),
            TeamMetric.newBuilder().setMetricName("Sentiment Trend").setValue(sentimentTrend).setTimestamp(now).build(),
            TeamMetric.newBuilder().setMetricName("Blocker Count").setValue(blockerCount.toFloat()).setTimestamp(now).build()
        )

        return GetTeamHealthResponse.newBuilder().addAllMetrics(metrics).build()
    }
}
