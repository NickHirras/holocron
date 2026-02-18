package io.holocron.ceremony;

import io.holocron.team.Team;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
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

@Path("/ceremonies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CeremonyResource {

    @GET
    public List<Ceremony> list() {
        return Ceremony.listAll();
    }

    @GET
    @Path("/{id}")
    public Ceremony get(@PathParam("id") Long id) {
        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony == null) {
            throw new NotFoundException();
        }
        return ceremony;
    }

    @POST
    @Transactional
    public Response create(Ceremony ceremony) {
        if (ceremony.team != null && ceremony.team.id != null) {
            ceremony.team = Team.findById(ceremony.team.id);
        }
        ceremony.persist();
        return Response.created(URI.create("/ceremonies/" + ceremony.id)).entity(ceremony).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Ceremony update(@PathParam("id") Long id, Ceremony ceremony) {
        Ceremony entity = Ceremony.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }

        entity.title = ceremony.title;
        entity.description = ceremony.description;
        entity.scheduleType = ceremony.scheduleType;
        entity.isActive = ceremony.isActive;
        entity.rrule = ceremony.rrule;
        entity.timezone = ceremony.timezone;
        entity.type = ceremony.type;

        if (ceremony.team != null && ceremony.team.id != null) {
            entity.team = Team.findById(ceremony.team.id);
        }

        return entity;
    }
}
