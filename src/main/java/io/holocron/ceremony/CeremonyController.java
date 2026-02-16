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
}
