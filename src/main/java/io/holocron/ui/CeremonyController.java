package io.holocron.ui;

import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyQuestion;
import io.holocron.ceremony.CeremonyType;
import io.holocron.team.Team;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/ceremonies")
@Authenticated
public class CeremonyController {

    @Inject
    @Location("ceremony/list.html")
    Template listTemplate;

    @Inject
    @Location("ceremony/form.html")
    Template formTemplate;

    @Inject
    @Location("ceremony/edit.html")
    Template editTemplate;

    // ── List all ceremonies ──────────────────────────────────────────

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@HeaderParam("HX-Request") boolean hxRequest) {
        List<Ceremony> ceremonies = Ceremony.listAll();
        List<Team> teams = Team.listAll();
        if (hxRequest) {
            return listTemplate.getFragment("ceremony_list_content")
                    .data("ceremonies", ceremonies).data("teams", teams);
        }
        return listTemplate.data("ceremonies", ceremonies).data("teams", teams);
    }

    // ── Create ceremony form ─────────────────────────────────────────

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm(@HeaderParam("HX-Request") boolean hxRequest) {
        List<Team> teams = Team.listAll();
        CeremonyType[] types = CeremonyType.values();
        if (hxRequest) {
            return formTemplate.getFragment("ceremony_form_content")
                    .data("ceremony", new Ceremony())
                    .data("teams", teams)
                    .data("types", types)
                    .data("action", "/ceremonies");
        }
        return formTemplate.data("ceremony", new Ceremony())
                .data("teams", teams)
                .data("types", types)
                .data("action", "/ceremonies");
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @io.holocron.audit.Audited(action = "CREATE_CEREMONY")
    public Response create(
            @FormParam("title") String title,
            @FormParam("description") String description,
            @FormParam("teamId") Long teamId,
            @FormParam("type") String type,
            @FormParam("scheduleType") String scheduleType,
            @FormParam("isActive") String isActive) {

        Team team = Team.findById(teamId);
        if (team == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Team not found").build();
        }

        Ceremony ceremony = new Ceremony();
        ceremony.title = title;
        ceremony.description = description;
        ceremony.team = team;
        ceremony.type = CeremonyType.valueOf(type);
        ceremony.scheduleType = scheduleType;
        ceremony.isActive = "on".equals(isActive) || "true".equals(isActive);
        ceremony.persist();

        // Redirect to the edit page so they can add questions
        return Response.seeOther(URI.create("/ceremonies/" + ceremony.id + "/edit")).build();
    }

    // ── Edit ceremony (with question management) ─────────────────────

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response editForm(@PathParam("id") Long id,
            @HeaderParam("HX-Request") boolean hxRequest) {
        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Ceremony not found").build();
        }

        List<CeremonyQuestion> questions = CeremonyQuestion
                .find("ceremony = ?1 order by sequence", ceremony).list();
        List<Team> teams = Team.listAll();
        CeremonyType[] types = CeremonyType.values();

        if (hxRequest) {
            return Response.ok(editTemplate.getFragment("ceremony_edit_content")
                    .data("ceremony", ceremony)
                    .data("questions", questions)
                    .data("teams", teams)
                    .data("types", types)).build();
        }
        return Response.ok(editTemplate.data("ceremony", ceremony)
                .data("questions", questions)
                .data("teams", teams)
                .data("types", types)).build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @io.holocron.audit.Audited(action = "UPDATE_CEREMONY")
    public Response update(
            @PathParam("id") Long id,
            @FormParam("title") String title,
            @FormParam("description") String description,
            @FormParam("teamId") Long teamId,
            @FormParam("type") String type,
            @FormParam("scheduleType") String scheduleType,
            @FormParam("isActive") String isActive) {

        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Ceremony not found").build();
        }

        Team team = Team.findById(teamId);
        if (team == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Team not found").build();
        }

        ceremony.title = title;
        ceremony.description = description;
        ceremony.team = team;
        ceremony.type = CeremonyType.valueOf(type);
        ceremony.scheduleType = scheduleType;
        ceremony.isActive = "on".equals(isActive) || "true".equals(isActive);

        return Response.seeOther(URI.create("/ceremonies/" + id + "/edit")).build();
    }

    // ── Delete ceremony ──────────────────────────────────────────────

    @POST
    @Path("/{id}/delete")
    @Transactional
    @io.holocron.audit.Audited(action = "DELETE_CEREMONY")
    public Response delete(@PathParam("id") Long id) {
        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony != null) {
            // Delete answers first, then responses, then questions, then ceremony
            CeremonyQuestion.delete("ceremony", ceremony);
            io.holocron.ceremony.CeremonyAnswer.delete(
                    "response in (select r from CeremonyResponse r where r.ceremony = ?1)", ceremony);
            io.holocron.ceremony.CeremonyResponse.delete("ceremony", ceremony);
            ceremony.delete();
        }
        return Response.seeOther(URI.create("/ceremonies")).build();
    }

    // ── Toggle active status (HTMX) ─────────────────────────────────

    @POST
    @Path("/{id}/toggle-active")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public String toggleActive(@PathParam("id") Long id) {
        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony != null) {
            ceremony.isActive = !ceremony.isActive;
        }
        // Return a small status badge for HTMX swap
        if (ceremony != null && ceremony.isActive) {
            return "<span class=\"status-badge active\">ACTIVE</span>";
        } else {
            return "<span class=\"status-badge inactive\">INACTIVE</span>";
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // QUESTION MANAGEMENT (HTMX-powered inline editing)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Add a new question to the ceremony. Returns the updated question list
     * as an HTML fragment.
     */
    @POST
    @Path("/{id}/questions")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance addQuestion(
            @PathParam("id") Long id,
            @FormParam("text") String text,
            @FormParam("type") String type,
            @FormParam("isRequired") String isRequired) {

        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony == null) {
            throw new jakarta.ws.rs.NotFoundException("Ceremony not found");
        }

        // Determine next sequence number
        Integer maxSeq = CeremonyQuestion.find(
                "select max(q.sequence) from CeremonyQuestion q where q.ceremony = ?1", ceremony)
                .project(Integer.class).firstResult();
        int nextSeq = (maxSeq != null) ? maxSeq + 1 : 1;

        CeremonyQuestion question = new CeremonyQuestion();
        question.ceremony = ceremony;
        question.text = text;
        question.type = type != null ? type : "TEXT";
        question.isRequired = "on".equals(isRequired) || "true".equals(isRequired);
        question.sequence = nextSeq;
        question.persist();

        // Return the full question list for the ceremony
        List<CeremonyQuestion> questions = CeremonyQuestion
                .find("ceremony = ?1 order by sequence", ceremony).list();
        return editTemplate.getFragment("question_list")
                .data("questions", questions)
                .data("ceremony", ceremony);
    }

    /**
     * Update a question in-place. Returns the updated row fragment.
     */
    @POST
    @Path("/{id}/questions/{qId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance updateQuestion(
            @PathParam("id") Long id,
            @PathParam("qId") Long qId,
            @FormParam("text") String text,
            @FormParam("type") String type,
            @FormParam("isRequired") String isRequired) {

        CeremonyQuestion question = CeremonyQuestion.findById(qId);
        if (question == null || !question.ceremony.id.equals(id)) {
            throw new jakarta.ws.rs.NotFoundException("Question not found");
        }

        question.text = text;
        question.type = type != null ? type : question.type;
        question.isRequired = "on".equals(isRequired) || "true".equals(isRequired);

        Ceremony ceremony = Ceremony.findById(id);
        List<CeremonyQuestion> questions = CeremonyQuestion
                .find("ceremony = ?1 order by sequence", ceremony).list();
        return editTemplate.getFragment("question_list")
                .data("questions", questions)
                .data("ceremony", ceremony);
    }

    /**
     * Delete a question. Returns the updated question list.
     */
    @POST
    @Path("/{id}/questions/{qId}/delete")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance deleteQuestion(
            @PathParam("id") Long id,
            @PathParam("qId") Long qId) {

        CeremonyQuestion question = CeremonyQuestion.findById(qId);
        if (question != null && question.ceremony.id.equals(id)) {
            // Delete any answers referencing this question first
            io.holocron.ceremony.CeremonyAnswer.delete("question", question);
            question.delete();

            // Re-sequence remaining questions
            Ceremony ceremony = Ceremony.findById(id);
            List<CeremonyQuestion> remaining = CeremonyQuestion
                    .find("ceremony = ?1 order by sequence", ceremony).list();
            int seq = 1;
            for (CeremonyQuestion q : remaining) {
                q.sequence = seq++;
            }
        }

        Ceremony ceremony = Ceremony.findById(id);
        List<CeremonyQuestion> questions = CeremonyQuestion
                .find("ceremony = ?1 order by sequence", ceremony).list();
        return editTemplate.getFragment("question_list")
                .data("questions", questions)
                .data("ceremony", ceremony);
    }

    /**
     * Move a question up or down. Returns the updated question list.
     */
    @POST
    @Path("/{id}/questions/{qId}/move/{direction}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance moveQuestion(
            @PathParam("id") Long id,
            @PathParam("qId") Long qId,
            @PathParam("direction") String direction) {

        Ceremony ceremony = Ceremony.findById(id);
        if (ceremony == null) {
            throw new jakarta.ws.rs.NotFoundException("Ceremony not found");
        }

        List<CeremonyQuestion> questions = CeremonyQuestion
                .find("ceremony = ?1 order by sequence", ceremony).list();

        // Find the question to move
        int idx = -1;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).id.equals(qId)) {
                idx = i;
                break;
            }
        }

        if (idx >= 0) {
            int swapIdx = "up".equals(direction) ? idx - 1 : idx + 1;
            if (swapIdx >= 0 && swapIdx < questions.size()) {
                // Swap sequences
                int tempSeq = questions.get(idx).sequence;
                questions.get(idx).sequence = questions.get(swapIdx).sequence;
                questions.get(swapIdx).sequence = tempSeq;
            }
        }

        // Re-fetch in new order
        questions = CeremonyQuestion
                .find("ceremony = ?1 order by sequence", ceremony).list();
        return editTemplate.getFragment("question_list")
                .data("questions", questions)
                .data("ceremony", ceremony);
    }
}
