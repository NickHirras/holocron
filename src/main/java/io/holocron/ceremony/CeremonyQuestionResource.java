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
import jakarta.ws.rs.BadRequestException;
import java.net.URI;
import java.util.List;

@Path("/ceremonies/{id}/questions")
@Produces(MediaType.APPLICATION_JSON)
public class CeremonyQuestionResource {

    @GET
    public List<CeremonyQuestion> list(@PathParam("id") Long ceremonyId) {
        return CeremonyQuestion.list("ceremony.id = ?1 order by sequence", ceremonyId);
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
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
    @Consumes(MediaType.APPLICATION_JSON)
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
    @Consumes(MediaType.WILDCARD)
    public Response delete(@PathParam("id") Long ceremonyId, @PathParam("questionId") Long questionId) {
        CeremonyQuestion entity = CeremonyQuestion.findById(questionId);
        if (entity == null || !entity.ceremony.id.equals(ceremonyId)) {
            throw new NotFoundException();
        }
        entity.delete();
        // Return 200 OK instead of 204 so HTMX performs the swap (deleting the element)
        return Response.ok().build();
    }

    @POST
    @Path("/reorder")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reorder(@PathParam("id") Long ceremonyId, ReorderRequest request) {
        List<Long> questionIds = request.values;
        // Verify all belong to ceremony
        long count = CeremonyQuestion.count("ceremony.id = ?1 and id in ?2", ceremonyId, questionIds);
        if (count != questionIds.size()) {
            throw new BadRequestException("Invalid question IDs");
        }

        // Update sequence
        for (int i = 0; i < questionIds.size(); i++) {
            CeremonyQuestion.update("sequence = ?1 where id = ?2", i + 1, questionIds.get(i));
        }

        return Response.ok().build();
    }

    @POST
    @Path("/{questionId}/move")
    @Transactional
    @Consumes(MediaType.WILDCARD)
    public Response move(@PathParam("id") Long ceremonyId,
            @PathParam("questionId") Long questionId,
            @jakarta.ws.rs.QueryParam("dir") String direction) {
        CeremonyQuestion question = CeremonyQuestion.findById(questionId);
        if (question == null || !question.ceremony.id.equals(ceremonyId))
            throw new NotFoundException();

        List<CeremonyQuestion> allQuestions = CeremonyQuestion.list("ceremony.id = ?1 order by sequence", ceremonyId);
        int currentIndex = -1;
        for (int i = 0; i < allQuestions.size(); i++) {
            if (allQuestions.get(i).id.equals(questionId)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1)
            return Response.ok().build(); // Should not happen

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

        return Response.ok().build();
    }

    public static class ReorderRequest {
        public List<Long> values;
    }
}
