package io.holocron.ui;

import io.holocron.team.Team;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/teams")
@Authenticated
public class TeamController {

    @Inject
    @Location("team/list.html")
    Template listTemplate;

    @Inject
    @Location("team/form.html")
    Template formTemplate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list() {
        List<Team> teams = Team.listAll();
        return listTemplate.data("teams", teams);
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm() {
        return formTemplate.data("team", new Team()).data("action", "/teams");
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @io.holocron.audit.Audited(action = "CREATE_TEAM")
    public Response create(@FormParam("name") String name, @FormParam("timezoneId") String timezoneId) {
        Team team = new Team();
        team.name = name;
        team.timezoneId = timezoneId;
        team.persist();
        return Response.seeOther(URI.create("/teams")).build();
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("id") Long id) {
        Team team = Team.findById(id);
        if (team == null) {
            throw new NotFoundException("Team not found");
        }
        return formTemplate.data("team", team).data("action", "/teams/" + id);
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @io.holocron.audit.Audited(action = "UPDATE_TEAM")
    public Response update(@PathParam("id") Long id, @FormParam("name") String name,
            @FormParam("timezoneId") String timezoneId) {
        Team team = Team.findById(id);
        if (team == null) {
            throw new NotFoundException("Team not found");
        }
        team.name = name;
        team.timezoneId = timezoneId;
        return Response.seeOther(URI.create("/teams")).build();
    }

    @POST
    @Path("/{id}/delete")
    @Transactional
    @io.holocron.audit.Audited(action = "DELETE_TEAM")
    public Response delete(@PathParam("id") Long id) {
        Team team = Team.findById(id);
        if (team != null) {
            // Note: Cascade delete logic might be needed for TeamMembers/Pulses if not
            // handled by DB
            // ensuring consistency. Team.deleteById(id) handles simple cases.
            // However, we should check if we need to manually clean up relations if
            // constraints exist.
            // For now, relying on standard delete. If FK constraints fail, we'll need to
            // handle it.
            // Given previous test failure on constraints, we might need to delete members
            // first?
            // Actually, let's keep it simple for now and rely on Panache.
            // If strictly needed: io.holocron.team.TeamMember.delete("team", team);
            // But let's see if we can do soft delete or if hard delete is OK. User said
            // CRUD.
            // We'll delete members first to allow team deletion safely.
            io.holocron.team.TeamMember.delete("team", team);
            team.delete();
        }
        return Response.seeOther(URI.create("/teams")).build();
    }
}
