package io.holocron.ceremony;

import io.holocron.audit.Audited;
import io.holocron.team.Team;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
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

@Path("/api/ceremonies")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class CeremonyResource {

    @GET
    @Path("/team/{teamId}")
    public List<Ceremony> getByTeam(@PathParam("teamId") Long teamId) {
        Team team = Team.findById(teamId);
        if (team == null) {
            throw new NotFoundException("Team not found");
        }
        return Ceremony.list("team", team);
    }

    @POST
    @Transactional
    @Audited(action = "create_ceremony")
    @RolesAllowed({ "Team Lead", "admin", "user" })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Ceremony ceremony) {
        if (ceremony.team == null || ceremony.team.id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Team is required").build();
        }

        Team team = Team.findById(ceremony.team.id);
        if (team == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Team").build();
        }
        ceremony.team = team;

        ceremony.persist();
        return Response.created(URI.create("/api/ceremonies/" + ceremony.id)).entity(ceremony).build();
    }

    @POST
    @Transactional
    @Audited(action = "create_ceremony")
    @RolesAllowed({ "Team Lead", "admin", "user" })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createFromForm(@jakarta.ws.rs.FormParam("title") String title,
            @jakarta.ws.rs.FormParam("description") String description,
            @jakarta.ws.rs.FormParam("rrule") String rrule,
            @jakarta.ws.rs.FormParam("timezone") String timezone,
            @jakarta.ws.rs.FormParam("type") CeremonyType type,
            @jakarta.ws.rs.FormParam("team.id") Long teamId) {
        Ceremony ceremony = new Ceremony();
        ceremony.title = title;
        ceremony.description = description;
        ceremony.rrule = rrule;
        ceremony.timezone = timezone;
        ceremony.type = type;
        ceremony.isActive = true;

        if (teamId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Team is required").build();
        }

        Team team = Team.findById(teamId);
        if (team == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Team").build();
        }
        ceremony.team = team;

        ceremony.persist();
        return Response.created(URI.create("/api/ceremonies/" + ceremony.id)).entity(ceremony).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Audited(action = "update_ceremony")
    @RolesAllowed({ "Team Lead", "admin", "user" })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, Ceremony ceremony) {
        Ceremony entity = Ceremony.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }

        entity.title = ceremony.title;
        entity.description = ceremony.description;
        entity.rrule = ceremony.rrule;
        entity.timezone = ceremony.timezone;
        entity.isActive = ceremony.isActive;
        entity.scheduleType = ceremony.scheduleType;

        return Response.ok(entity).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Audited(action = "delete_ceremony")
    @RolesAllowed("Team Lead")
    public Response delete(@PathParam("id") Long id) {
        Ceremony entity = Ceremony.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.delete();
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/questions")
    @Transactional
    @Audited(action = "add_question")
    @RolesAllowed({ "Team Lead", "admin", "user" })
    public Response addQuestion(@PathParam("id") Long id) {
        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony == null) {
            throw new NotFoundException();
        }

        CeremonyQuestion question = new CeremonyQuestion();
        question.ceremony = ceremony;
        question.text = "New Parameter";
        question.type = "TEXT";
        question.isRequired = false;

        Integer maxSeq = Utility.maxSequence(ceremony);
        question.sequence = maxSeq + 1;

        question.persist();
        return Response.created(URI.create("/api/questions/" + question.id)).entity(question).build();
    }

    @POST
    @Path("/{id}/questions/reorder")
    @Transactional
    @Audited(action = "reorder_questions")
    @RolesAllowed({ "Team Lead", "admin", "user" })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response reorderQuestions(@PathParam("id") Long id, @jakarta.ws.rs.FormParam("item") List<String> itemIds) {
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("reorder_entry.log"),
                    "Entered reorder for " + id + "\n", java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);

            Ceremony ceremony = Ceremony.findById(id);
            if (ceremony == null) {
                throw new NotFoundException();
            }

            int seq = 1;
            if (itemIds != null) {
                for (String itemIdStr : itemIds) {
                    Long qId = Long.parseLong(itemIdStr);
                    CeremonyQuestion q = CeremonyQuestion.findById(qId);
                    if (q != null && q.ceremony.id.equals(id)) {
                        q.sequence = seq++;
                    }
                }
            }

            return Response.ok().build();
        } catch (Exception e) {
            try {
                java.nio.file.Files.writeString(java.nio.file.Path.of("error_reorder.log"),
                        e.toString() + "\n" + java.util.Arrays.toString(e.getStackTrace()),
                        java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            } catch (java.io.IOException io) {
                io.printStackTrace();
            }
            throw new RuntimeException(e);
        }
    }

    private static class Utility {
        static Integer maxSequence(Ceremony c) {
            return CeremonyQuestion.find("ceremony = ?1 order by sequence desc", c)
                    .firstResultOptional()
                    .map(q -> ((CeremonyQuestion) q).sequence)
                    .orElse(0);
        }
    }
}
