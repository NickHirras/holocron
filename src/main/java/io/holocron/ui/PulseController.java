package io.holocron.ui;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyQuestion;
import io.holocron.pulse.PulseService;
import io.holocron.team.Team;
import io.holocron.user.User;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
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
public class PulseController {

    @Inject
    PulseService pulseService;

    @Inject
    Template pulse;

    @GET
    public Response index() {
        // For now, default to "Engineering"
        // In real app, would use logged-in user's team
        Team team = Team.find("name", "Engineering").firstResult();
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Team not found").build();
        }
        return Response.seeOther(URI.create("/pulse/" + team.id)).build();
    }

    @GET
    @Path("/{teamId}")
    @Produces(MediaType.TEXT_HTML)
    public Response show(@PathParam("teamId") Long teamId) {
        Team team = Team.findById(teamId);
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Ceremony> activePulse = pulseService.findActivePulse(team);
        if (activePulse.isEmpty()) {
            return Response.ok(pulse.data("team", team).data("noActivePulse", true)).build();
        }

        Ceremony ceremony = activePulse.get();
        // Mock user for now - assume Alice
        User user = User.findByEmail("alice@holocron.io");

        if (pulseService.hasSubmitted(ceremony, user, java.time.LocalDate.now())) {
            return Response.ok(pulse.data("team", team)
                    .data("hasSubmitted", true)
                    .data("noActivePulse", false)).build();
        }

        List<CeremonyQuestion> questions = CeremonyQuestion.find("ceremony = ?1 order by sequence", ceremony).list();

        return Response.ok(pulse.data("team", team)
                .data("ceremony", ceremony)
                .data("questions", questions)
                .data("user", user)
                .data("noActivePulse", false)
                .data("hasSubmitted", false)).build();
    }

    @POST
    @Path("/{teamId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response submit(@PathParam("teamId") Long teamId, MultivaluedMap<String, String> formParams) {
        Team team = Team.findById(teamId);
        if (team == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        Optional<Ceremony> activePulse = pulseService.findActivePulse(team);
        if (activePulse.isEmpty())
            return Response.status(Response.Status.BAD_REQUEST).build();

        Ceremony ceremony = activePulse.get();
        // Mock user - assume Alice
        User user = User.findByEmail("alice@holocron.io");

        String comments = formParams.getFirst("comments");
        Map<Long, String> answers = new HashMap<>();

        for (String key : formParams.keySet()) {
            if (key.startsWith("question_")) {
                String questionIdStr = key.substring("question_".length());
                try {
                    Long qId = Long.parseLong(questionIdStr);
                    answers.put(qId, formParams.getFirst(key));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        try {
            pulseService.submitResponse(ceremony, user, answers, comments);
        } catch (IllegalStateException e) {
            // Already submitted
            return Response.seeOther(URI.create("/pulse/" + teamId)).build();
        }

        return Response.seeOther(URI.create("/pulse/" + teamId + "?submitted=true")).build();
    }
}
