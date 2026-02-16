package io.holocron.ceremony;

import io.holocron.audit.Audited;
import io.holocron.team.Team;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
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

@Path("/api/ceremonies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
    @RolesAllowed({ "Team Lead", "admin", "user" }) // Assumes "Team Lead" role is mapped
    public Response create(Ceremony ceremony) {
        if (ceremony.team == null || ceremony.team.id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Team is required").build();
        }

        // Ensure team exists
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
        // HTMX expects a redirect or partial. Since we return Created 201 with
        // Location,
        // HTMX will follow if configured, or we can return the JSON representation.
        // For our form.html, we use hx-on::after-request to read response.id.
        return Response.created(URI.create("/api/ceremonies/" + ceremony.id)).entity(ceremony).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Audited(action = "update_ceremony")
    @RolesAllowed({ "Team Lead", "admin", "user" })
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

        // Validation for RRule could go here

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
}
