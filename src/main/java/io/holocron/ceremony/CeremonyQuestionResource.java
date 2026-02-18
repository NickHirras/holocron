package io.holocron.ceremony;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/ceremonies/{id}/questions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CeremonyQuestionResource {

    @GET
    public List<CeremonyQuestion> list(@PathParam("id") Long ceremonyId) {
        return CeremonyQuestion.list("ceremony.id = ?1 order by sequence", ceremonyId);
    }

    @POST
    @Transactional
    public Response create(@PathParam("id") Long ceremonyId, CeremonyQuestion question) {
        Ceremony ceremony = Ceremony.findById(ceremonyId);
        if (ceremony == null) {
            throw new NotFoundException();
        }
        question.ceremony = ceremony;

        // Auto-assign sequence to end of list
        long count = CeremonyQuestion.count("ceremony.id", ceremonyId);
        question.sequence = (int) count + 1;

        question.persist();
        return Response.created(URI.create("/ceremonies/" + ceremonyId + "/questions/" + question.id)).entity(question)
                .build();
    }

    @PUT
    @Path("/{questionId}")
    @Transactional
    public CeremonyQuestion update(@PathParam("id") Long ceremonyId, @PathParam("questionId") Long questionId,
            CeremonyQuestion question) {
        CeremonyQuestion entity = CeremonyQuestion.findById(questionId);
        if (entity == null || !entity.ceremony.id.equals(ceremonyId)) {
            throw new NotFoundException();
        }

        entity.text = question.text;
        entity.type = question.type;
        entity.isRequired = question.isRequired;

        return entity;
    }

    @DELETE
    @Path("/{questionId}")
    @Transactional
    public Response delete(@PathParam("id") Long ceremonyId, @PathParam("questionId") Long questionId) {
        CeremonyQuestion entity = CeremonyQuestion.findById(questionId);
        if (entity == null || !entity.ceremony.id.equals(ceremonyId)) {
            throw new NotFoundException();
        }
        entity.delete();
        return Response.noContent().build();
    }

    @POST
    @Path("/reorder")
    @Transactional
    public Response reorder(@PathParam("id") Long ceremonyId, List<Long> questionIds) {
        // Verify ceremony exists
        Ceremony ceremony = Ceremony.findById(ceremonyId);
        if (ceremony == null) {
            throw new NotFoundException();
        }

        for (int i = 0; i < questionIds.size(); i++) {
            Long qId = questionIds.get(i);
            CeremonyQuestion question = CeremonyQuestion.findById(qId);
            if (question != null && question.ceremony.id.equals(ceremonyId)) {
                question.sequence = i + 1; // 1-based sequence
                // Implicit persistence due to transaction
            } else {
                // Skip or error? Design review says "sorting failures" should revert UI.
                // Ideally we validate all first, but finding by ID inside loop is okay for
                // small sets.
                // Providing robustness: only update if belongs to ceremony.
            }
        }

        return Response.ok().build();
    }

    @POST
    @Path("/{questionId}/move")
    @Transactional
    public Response move(@PathParam("id") Long ceremonyId,
            @PathParam("questionId") Long questionId,
            @jakarta.ws.rs.QueryParam("dir") String direction) {

        CeremonyQuestion question = CeremonyQuestion.findById(questionId);
        if (question == null || !question.ceremony.id.equals(ceremonyId)) {
            throw new NotFoundException();
        }

        List<CeremonyQuestion> allQuestions = CeremonyQuestion.list("ceremony.id = ?1 order by sequence", ceremonyId);
        int currentIndex = allQuestions.indexOf(question);

        if (currentIndex == -1)
            return Response.status(Response.Status.BAD_REQUEST).build();

        if ("UP".equalsIgnoreCase(direction) && currentIndex > 0) {
            CeremonyQuestion other = allQuestions.get(currentIndex - 1);
            int temp = question.sequence;
            question.sequence = other.sequence;
            other.sequence = temp;
        } else if ("DOWN".equalsIgnoreCase(direction) && currentIndex < allQuestions.size() - 1) {
            CeremonyQuestion other = allQuestions.get(currentIndex + 1);
            int temp = question.sequence;
            question.sequence = other.sequence;
            other.sequence = temp;
        }

        return Response.ok().header("HX-Trigger", "questionsChanged").build();
    }
}
