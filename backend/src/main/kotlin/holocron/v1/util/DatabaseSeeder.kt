package holocron.v1.util

import com.google.protobuf.Timestamp
import holocron.v1.*
import holocron.v1.repository.*
import io.viascom.nanoid.NanoId
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class DatabaseSeeder(
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val templateRepository: CeremonyTemplateRepository,
    private val responseRepository: CeremonyResponseRepository
) {
    suspend fun seedIfEmpty() {
        val nickEmail = "nick@nebula.io"
        val memberships = teamRepository.getUserMemberships(nickEmail)
        
        if (memberships.isNotEmpty()) {
            println("DatabaseSeeder: Seeding skipped. User Nick already belongs to a team.")
            return
        }
        
        println("DatabaseSeeder: Starting initial database seed...")

        // 1. Create Users
        val users = listOf(
            "nick@nebula.io", "maria@nebula.io", "elena@nebula.io", 
            "david@nebula.io", "sarah@nebula.io", "marcus@nebula.io", 
            "jordan@nebula.io", "sam@nebula.io"
        )
        
        users.forEach { email -> userRepository.findOrCreate(email) }
        
        // 2. Create "Nebula Infrastructure" Team
        val (team, _) = teamRepository.createTeam("Nebula Infrastructure", "nick@nebula.io")
        
        // 3. Add members & set Maria as leader
        val otherUsers = users.drop(1)
        otherUsers.forEach { email -> 
            teamRepository.joinTeam(team.id, email)
        }
        teamRepository.updateRole(team.id, "maria@nebula.io", TeamMembership.Role.ROLE_LEADER)

        // 4. Create Ceremony Templates
        val dailyStandup = createStandupTemplate(team.id)
        templateRepository.save(dailyStandup)
        
        val sprintRetro = createRetroTemplate(team.id)
        templateRepository.save(sprintRetro)

        // 5. Generate Responses over the last 28 days
        generateResponses(team, dailyStandup, 28, users, isDaily = true)
        generateResponses(team, sprintRetro, 4, users, isDaily = false)
        
        println("DatabaseSeeder: Seeding complete.")
    }

    private fun createStandupTemplate(teamId: String): CeremonyTemplate {
        val now = Timestamp.newBuilder().setSeconds(Instant.now().epochSecond).build()
        val templateId = NanoId.generate(12, "23456789abcdefghjkmnpqrstuvwxyz")
        return CeremonyTemplate.newBuilder()
            .setId(templateId)
            .setTeamId(teamId)
            .setTitle("Daily Standup")
            .setDescription("What are you working on today?")
            .setCreatedAt(now)
            .setUpdatedAt(now)
            .setCreatorId("nick@nebula.io")
            .setIsPublic(false)
            .setFacilitationSettings(FacilitationSettings.newBuilder().setIsAnonymized(false).setResponsesVisible(true))
            .addItems(
                Item.newBuilder().setItemId(NanoId.generate(12)).setTitle("General Sentiment").setQuestionItem(QuestionItem.newBuilder()
                    .setQuestion(Question.newBuilder().setQuestionId("q_1_standup").setRequired(true)
                        .setScaleQuestion(ScaleQuestion.newBuilder().setLow(1).setHigh(10).setLowLabel("Terrible").setHighLabel("Great")))
                )
            )
            .addItems(
                Item.newBuilder().setItemId(NanoId.generate(12)).setTitle("Accomplishments").setQuestionItem(QuestionItem.newBuilder()
                    .setQuestion(Question.newBuilder().setQuestionId("q_2_standup").setRequired(true)
                        .setTextQuestion(TextQuestion.newBuilder().setParagraph(true)))
                )
            )
            .addItems(
                Item.newBuilder().setItemId(NanoId.generate(12)).setTitle("Today's Plan").setQuestionItem(QuestionItem.newBuilder()
                    .setQuestion(Question.newBuilder().setQuestionId("q_3_standup").setRequired(true)
                        .setTextQuestion(TextQuestion.newBuilder().setParagraph(true)))
                )
            )
            .addItems(
                Item.newBuilder().setItemId(NanoId.generate(12)).setTitle("Blockers").setQuestionItem(QuestionItem.newBuilder()
                    .setQuestion(Question.newBuilder().setQuestionId("q_4_standup").setRequired(false)
                        .setTextQuestion(TextQuestion.newBuilder().setParagraph(true)))
                )
            )
            .build()
    }

    private fun createRetroTemplate(teamId: String): CeremonyTemplate {
        val now = Timestamp.newBuilder().setSeconds(Instant.now().epochSecond).build()
        val templateId = NanoId.generate(12, "23456789abcdefghjkmnpqrstuvwxyz")
        return CeremonyTemplate.newBuilder()
            .setId(templateId)
            .setTeamId(teamId)
            .setTitle("Sprint Retro")
            .setDescription("Reflecting on the past sprint.")
            .setCreatedAt(now)
            .setUpdatedAt(now)
            .setCreatorId("nick@nebula.io")
            .setIsPublic(false)
            .setFacilitationSettings(FacilitationSettings.newBuilder().setIsAnonymized(true).setResponsesVisible(false))
            .addItems(
                Item.newBuilder().setItemId(NanoId.generate(12)).setTitle("Sprint Sentiment").setQuestionItem(QuestionItem.newBuilder()
                    .setQuestion(Question.newBuilder().setQuestionId("q_1_retro").setRequired(true)
                        .setScaleQuestion(ScaleQuestion.newBuilder().setLow(1).setHigh(10).setLowLabel("Tough").setHighLabel("Smooth")))
                )
            )
            .addItems(
                Item.newBuilder().setItemId(NanoId.generate(12)).setTitle("What went well?").setQuestionItem(QuestionItem.newBuilder()
                    .setQuestion(Question.newBuilder().setQuestionId("q_2_retro").setRequired(true)
                        .setTextQuestion(TextQuestion.newBuilder().setParagraph(true)))
                )
            )
            .addItems(
                Item.newBuilder().setItemId(NanoId.generate(12)).setTitle("What went wrong?").setQuestionItem(QuestionItem.newBuilder()
                    .setQuestion(Question.newBuilder().setQuestionId("q_3_retro").setRequired(true)
                        .setTextQuestion(TextQuestion.newBuilder().setParagraph(true)))
                )
            )
            .addItems(
                Item.newBuilder().setItemId(NanoId.generate(12)).setTitle("Action Items").setQuestionItem(QuestionItem.newBuilder()
                    .setQuestion(Question.newBuilder().setQuestionId("q_4_retro").setRequired(false)
                        .setTextQuestion(TextQuestion.newBuilder().setParagraph(true)))
                )
            )
            .build()
    }

    private suspend fun generateResponses(team: Team, template: CeremonyTemplate, count: Int, users: List<String>, isDaily: Boolean) {
        val now = Instant.now()

        val orbitAccomplishments = listOf(
            "Worked on Project Orbit deployment configurations.",
            "Merged the PR for the new orchestration pipeline.",
            "Debugged the ingress routing issues on staging.",
            "Updated the Helm charts for the metrics server.",
            "Wrote unit tests for the deployment orchestrator.",
            "Finished migrating the legacy data to the new schema.",
            "Reviewed David's PR on the authorization middleware.",
            "Automated the DB backup scripts.",
            "Investigated the performance regressions in the API gateway.",
            "Paired with Elena on the CI/CD pipeline improvements."
        )

        val orbitPlans = listOf(
            "Continuing Project Orbit setup.",
            "Planning to tackle the Kubernetes secrets management.",
            "Going to focus on the documentation for the new API endpoints.",
            "Starting the implementation of the new dashboard widgets.",
            "Will be working on optimizing the database queries.",
            "Aiming to finish the integration tests today.",
            "Need to sync with the frontend team regarding the API contract.",
            "Working on the monitoring alerts configuration.",
            "Deploying the hotfix to production.",
            "Refactoring the authentication service."
        )

        val retroGood = listOf(
            "The deployment went smoothly without any downtime.",
            "Great collaboration with the frontend team this sprint.",
            "We finally shipped the new orchestration pipeline!",
            "Code reviews were very thorough and helpful.",
            "The new CI/CD improvements are saving us a lot of time.",
            "I felt we had a good balance of feature work and tech debt."
        )

        val retroBad = listOf(
            "The orchestration failures cost us time.",
            "We had too many context switches this sprint.",
            "The staging environment was unstable for a couple of days.",
            "I feel like we under-communicated regarding the API changes.",
            "We underestimated the complexity of the database migration.",
            "Still struggling with some obsolete documentation."
        )

        val blockersList = listOf(
            "I am stuck on the ingress rules, acting as a blocker.",
            "Waiting on approval from security before I can merge.",
            "Blocked by the infrastructure outage yesterday.",
            "Need clarification on the requirements for the new feature.",
            "My local environment is completely broken, stuck."
        )

        val actionItemsList = listOf(
            "Need to schedule a meeting with networking.",
            "Let's document the deployment process better.",
            "Investigate the root cause of the database timeouts.",
            "Allocate more time for testing in the next sprint.",
            "Set up a pairing session to share knowledge on the new orchestrator."
        )
        
        for (i in 0 until count) {
            val daysAgo = if (isDaily) count - i else (count - i) * 7
            val submissionTime = now.minus(daysAgo.toLong(), ChronoUnit.DAYS).plus(Random.nextLong(0, 12), ChronoUnit.HOURS)
            
            val respondedUsers = users.filter { Random.nextBoolean() || it == "nick@nebula.io" }
            
            for (userEmail in respondedUsers) {
                val q1 = if (isDaily) "q_1_standup" else "q_1_retro"
                val q2 = if (isDaily) "q_2_standup" else "q_2_retro"
                val q3 = if (isDaily) "q_3_standup" else "q_3_retro"
                val q4 = if (isDaily) "q_4_standup" else "q_4_retro"
                
                val hasBlocker = Random.nextInt(100) < 15
                val sentiment = if (hasBlocker) Random.nextInt(2, 6) else Random.nextInt(7, 11)
                
                val builder = CeremonyResponse.newBuilder()
                    .setResponseId(NanoId.generate(12, "23456789abcdefghjkmnpqrstuvwxyz"))
                    .setCeremonyTemplateId(template.id)
                    .setUserId(userEmail)
                    .setSubmittedAt(Timestamp.newBuilder().setSeconds(submissionTime.epochSecond).build())
                    
                builder.putAnswers(q1, Answer.newBuilder().setScaleAnswer(ScaleAnswer.newBuilder().setValue(sentiment)).build())
                
                val ans2 = if (isDaily) orbitAccomplishments.random() else retroGood.random()
                builder.putAnswers(q2, Answer.newBuilder().setTextAnswer(TextAnswer.newBuilder().setValue(ans2)).build())
                
                val ans3 = if (isDaily) orbitPlans.random() else retroBad.random()
                builder.putAnswers(q3, Answer.newBuilder().setTextAnswer(TextAnswer.newBuilder().setValue(ans3)).build())
                
                if (hasBlocker || !isDaily) {
                    val blockerText = if (hasBlocker) blockersList.random() else actionItemsList.random()
                    builder.putAnswers(q4, Answer.newBuilder().setTextAnswer(TextAnswer.newBuilder().setValue(blockerText)).build())
                }
                
                responseRepository.save(builder.build())
            }
        }
    }
}
