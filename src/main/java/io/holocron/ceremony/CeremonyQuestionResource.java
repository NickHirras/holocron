package io.holocron.ceremony;

import io.holocron.audit.Audited;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.GET;

@Path("/api/questions")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class CeremonyQuestionResource {

    @PUT
    @Path("/{id}")
    @Transactional
    @Audited(action = "update_question")
    @RolesAllowed({ "Team Lead", "admin", "user" })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, CeremonyQuestion question) {
        CeremonyQuestion entity = CeremonyQuestion.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }

        entity.text = question.text;
        entity.type = question.type;
        entity.isRequired = question.isRequired;
        entity.sequence = question.sequence;

        return Response.ok(entity).build();
    }

    @PUT
    @Path("/{id}/form")
    @Transactional
    @Audited(action = "update_question")
    @RolesAllowed({ "Team Lead", "admin", "user" })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateFromForm(@PathParam("id") Long id,
            @FormParam("text") String text,
            @FormParam("type") String type,
            @FormParam("isRequired") boolean isRequired) {
        CeremonyQuestion entity = CeremonyQuestion.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }

        entity.text = text;
        entity.type = type;
        entity.isRequired = isRequired;

        return Response.ok(entity).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Audited(action = "delete_question")
    @RolesAllowed({ "Team Lead", "admin", "user" })
    @Consumes(MediaType.WILDCARD)
    public Response delete(@PathParam("id") Long id) {
        try {
            // Logging removed for cleaner code, relies on success or Quarkus logs
            CeremonyQuestion entity = CeremonyQuestion.findById(id);
            if (entity == null) {
                throw new NotFoundException();
            }
            entity.delete();
            return Response.noContent().build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/{id}/delete")
    @Transactional
    @Audited(action = "delete_question")
    @RolesAllowed({ "Team Lead", "admin", "user" })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deletePost(@PathParam("id") Long id) {
        return delete(id);
    }
}
