package io.holocron.ceremony;

import io.holocron.team.Team;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/sect/ceremonies")
@Authenticated
public class CeremonyController {

    @Inject
    @Location("ceremony/form.html")
    Template formTemplate;

    @Inject
    @Location("ceremony/questions.html")
    Template questionsTemplate;

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance newCeremony() {
        return formTemplate.data("ceremony", new Ceremony());
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance edit(@PathParam("id") Long id) {
        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony == null) {
            throw new jakarta.ws.rs.NotFoundException();
        }
        return formTemplate.data("ceremony", ceremony);
    }

    @GET
    @Path("/{id}/questions")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance questions(@PathParam("id") Long id) {
        Ceremony ceremony = Ceremony.findById(id);
        return questionsTemplate.data("ceremony", ceremony)
                .data("questions", CeremonyQuestion.list("ceremony", ceremony));
    }

    @Inject
    @Location("ceremony/question_item.html")
    Template questionItemTemplate;

    @jakarta.ws.rs.POST
    @Path("/{id}/questions")
    @Produces(MediaType.TEXT_HTML)
    @jakarta.transaction.Transactional
    public TemplateInstance addQuestion(@PathParam("id") Long id) {
        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony == null) {
            throw new jakarta.ws.rs.NotFoundException();
        }

        CeremonyQuestion question = new CeremonyQuestion();
        question.ceremony = ceremony;
        question.text = "New Parameter";
        question.type = "TEXT";
        question.isRequired = false;

        // Simple max sequence logic
        Integer maxSeq = CeremonyQuestion.find("ceremony = ?1 order by sequence desc", ceremony)
                .firstResultOptional()
                .map(q -> ((CeremonyQuestion) q).sequence)
                .orElse(0);
        question.sequence = maxSeq + 1;

        question.persist();

        return questionItemTemplate.data("question", question);
    }
}
