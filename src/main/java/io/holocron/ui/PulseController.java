package io.holocron.ui;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyQuestion;
import io.holocron.pulse.PulseService;
import io.holocron.team.Team;
import io.holocron.user.User;
import io.holocron.user.UserStats;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/pulse")
@io.quarkus.security.Authenticated
public class PulseController {

    @Inject
    io.quarkus.security.identity.SecurityIdentity identity;

    @Inject
    PulseService pulseService;

    @Inject
    Template pulse;

    @Inject
    Template pulse_question_step;

    @Inject
    Template pulse_comments_step;

    @Inject
    Template pulse_transmission_complete;

    @GET
    public Response index() {
        Team team = Team.find("name", "Engineering").firstResult();
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Team not found").build();
        }
        return Response.seeOther(URI.create("/pulse/" + team.id)).build();
    }

    @GET
    @Path("/{teamId}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response show(@PathParam("teamId") Long teamId) {
        Team team = Team.findById(teamId);
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String email = identity.getPrincipal().getName();
        User user = User.findByEmail(email);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not found in roster").build();
        }

        Optional<Ceremony> activePulse = pulseService.findActivePulse(team);
        if (activePulse.isEmpty()) {
            return Response.ok(pulse.data("team", team)
                    .data("user", user)
                    .data("noActivePulse", true)
                    .data("hasSubmitted", false)
                    .data("totalSteps", 0)).build();
        }

        Ceremony ceremony = activePulse.get();

        if (pulseService.hasSubmitted(ceremony, user, java.time.LocalDate.now())) {
            return Response.ok(pulse.data("team", team)
                    .data("user", user)
                    .data("hasSubmitted", true)
                    .data("noActivePulse", false)
                    .data("totalSteps", 0)).build();
        }

        List<CeremonyQuestion> questions = CeremonyQuestion
                .find("ceremony = ?1 order by sequence", ceremony).list();
        List<Integer> scaleValues = List.of(1, 2, 3, 4, 5);
        // totalSteps = questions + 1 (comments step)
        int totalSteps = questions.size() + 1;

        ensureStats(user);

        return Response.ok(pulse.data("team", team)
                .data("user", user)
                .data("ceremony", ceremony)
                .data("questions", questions)
                .data("scaleValues", scaleValues)
                .data("noActivePulse", false)
                .data("hasSubmitted", false)
                .data("totalSteps", totalSteps)).build();
    }

    /**
     * HTMX endpoint: returns the next question step as an HTML fragment.
     */
    @GET
    @Path("/{teamId}/step/{stepIndex}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response questionStep(
            @PathParam("teamId") Long teamId,
            @PathParam("stepIndex") int stepIndex) {
        Team team = Team.findById(teamId);
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Ceremony> activePulse = pulseService.findActivePulse(team);
        if (activePulse.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Ceremony ceremony = activePulse.get();
        List<CeremonyQuestion> questions = CeremonyQuestion
                .find("ceremony = ?1 order by sequence", ceremony).list();
        int totalSteps = questions.size() + 1; // +1 for comments

        // If stepIndex >= questions.size(), show comments step
        if (stepIndex >= questions.size()) {
            return Response.ok(pulse_comments_step
                    .data("stepIndex", stepIndex)
                    .data("totalSteps", totalSteps)
                    .data("teamId", teamId)).build();
        }

        CeremonyQuestion question = questions.get(stepIndex);
        List<Integer> scaleValues = List.of(1, 2, 3, 4, 5);

        return Response.ok(pulse_question_step
                .data("question", question)
                .data("scaleValues", scaleValues)
                .data("stepIndex", stepIndex)
                .data("totalSteps", totalSteps)
                .data("teamId", teamId)).build();
    }

    @POST
    @Path("/{teamId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response submit(
            @PathParam("teamId") Long teamId,
            @HeaderParam("HX-Request") String hxRequest,
            MultivaluedMap<String, String> formParams) {
        Team team = Team.findById(teamId);
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("<div class='error'>Team not found</div>").build();
        }

        String email = identity.getPrincipal().getName();
        User user = User.findByEmail(email);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("<div class='error'>User not found</div>").build();
        }

        Optional<Ceremony> activePulse = pulseService.findActivePulse(team);
        if (activePulse.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("<div class='error'>No active pulse</div>").build();
        }

        Ceremony ceremony = activePulse.get();

        String comments = formParams.getFirst("comments");
        Map<Long, String> answers = new HashMap<>();

        for (String key : formParams.keySet()) {
            if (key.startsWith("question_")) {
                try {
                    String questionIdStr = key.substring("question_".length());
                    Long qId = Long.parseLong(questionIdStr);
                    answers.put(qId, formParams.getFirst(key));
                } catch (NumberFormatException e) {
                    // ignore malformed keys
                }
            }
        }

        try {
            pulseService.submitResponse(ceremony, user, answers, comments);
        } catch (IllegalStateException e) {
            // Already submitted — fall through to success state
        }

        user = User.findById(user.id);
        ensureStats(user);

        // Return the "Transmission Complete" fragment
        return Response.ok(pulse_transmission_complete
                .data("user", user)).build();
    }

    private void ensureStats(User user) {
        if (user.stats == null) {
            UserStats stats = UserStats.findByUser(user);
            if (stats == null) {
                stats = new UserStats();
                stats.user = user;
                stats.persist();
            }
            user.stats = stats;
        }
    }
}
