package io.holocron.ui;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyQuestion;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import java.util.List;

@Path("/ceremonies/builder")
@Authenticated
public class CeremonyBuilderController {

    @Inject
    @io.quarkus.qute.Location("ceremony/builder.html")
    Template builder;

    @Inject
    @io.quarkus.qute.Location("ceremony/_question_card.html")
    Template questionCard;

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance builder(@PathParam("id") Long id) {
        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony == null) {
            throw new NotFoundException();
        }

        List<CeremonyQuestion> questions = CeremonyQuestion.list("ceremony.id = ?1 order by sequence", id);

        return builder.data("ceremony", ceremony)
                .data("questions", questions);
    }

    @GET
    @Path("/{id}/questions")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance questionsList(@PathParam("id") Long id) {
        List<CeremonyQuestion> questions = CeremonyQuestion.list("ceremony.id = ?1 order by sequence", id);
        return builder.getFragment("question_list").data("questions", questions).data("ceremony",
                Ceremony.findById(id));
    }

    @POST
    @Path("/{id}/questions")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance addQuestion(@PathParam("id") Long id, @FormParam("text") String text,
            @FormParam("type") String type) {
        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony == null)
            throw new NotFoundException();

        CeremonyQuestion question = new CeremonyQuestion();
        question.ceremony = ceremony;
        question.text = text;
        question.type = type != null ? type : "TEXT"; // Default
        question.isRequired = false;
        long count = CeremonyQuestion.count("ceremony.id", id);
        question.sequence = (int) count + 1;
        question.persist();

        return questionCard.data("question", question);
    }

    @PUT
    @Path("/{id}/questions/{qId}")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance updateQuestion(@PathParam("id") Long id, @PathParam("qId") Long qId,
            @FormParam("text") String text,
            @FormParam("type") String type,
            @FormParam("isRequired") String isRequiredStr) {

        CeremonyQuestion question = CeremonyQuestion.findById(qId);
        if (question == null || !question.ceremony.id.equals(id))
            throw new NotFoundException();

        if (text != null)
            question.text = text;
        if (type != null)
            question.type = type;
        question.isRequired = isRequiredStr != null && (isRequiredStr.equals("on") || isRequiredStr.equals("true"));

        // Return the updated card fragment
        return questionCard.data("question", question);
    }

    @DELETE
    @Path("/{id}/questions/{qId}")
    @Transactional
    public Response deleteQuestion(@PathParam("id") Long id, @PathParam("qId") Long qId) {
        CeremonyQuestion question = CeremonyQuestion.findById(qId);
        if (question == null || !question.ceremony.id.equals(id))
            throw new NotFoundException();

        question.delete();
        // Return empty 200 OK to remove element if swap is outerHTML
        return Response.ok().build();
    }
}
