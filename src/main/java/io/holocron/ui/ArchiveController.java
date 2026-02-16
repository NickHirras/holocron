package io.holocron.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.holocron.archive.ArchiveDTO;
import io.holocron.archive.Artifact;
import io.holocron.archive.ArtifactService;
import io.holocron.ceremony.Ceremony;
import io.holocron.team.Team;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;

@Path("/archives")
public class ArchiveController {

    @Inject
    ArtifactService artifactService;

    @Inject
    @Location("archive.html")
    Template archiveTemplate;

    @Inject
    @Location("archive_detail.html")
    Template archiveDetailTemplate;

    @Inject
    @Location("archive_content.html")
    Template archiveContentTemplate;

    @Inject
    ObjectMapper objectMapper;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@jakarta.ws.rs.HeaderParam("HX-Request") boolean hxRequest) {
        // TODO: Get actual logged in user/team.
        Team team = Team.findAll().firstResult();
        List<Artifact> artifacts = artifactService.findRecentArtifacts(team);

        TemplateInstance instance = hxRequest ? archiveContentTemplate.instance() : archiveTemplate.instance();
        return instance.data("artifacts", artifacts).data("selectedArtifact", null);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance detail(@PathParam("id") Long id) {
        Artifact artifact = Artifact.findById(id);
        if (artifact == null) {
            return io.quarkus.qute.Qute.fmt("Artifact not found").instance();
        }

        ArchiveDTO dto = null;
        try {
            if (artifact.summaryJson != null && !artifact.summaryJson.isEmpty()) {
                dto = objectMapper.readValue(artifact.summaryJson, ArchiveDTO.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return archiveDetailTemplate
                .data("selectedArtifact", artifact)
                .data("archiveDto", dto);
    }

    // Dev endpoint to trigger generation
    @POST
    @Path("/generate")
    @Transactional
    public void generate(@QueryParam("ceremonyId") Long ceremonyId) {
        Ceremony ceremony = Ceremony.findById(ceremonyId);
        if (ceremony != null) {
            artifactService.generateArtifact(ceremony, LocalDate.now(), LocalDate.now());
        }
    }
}
