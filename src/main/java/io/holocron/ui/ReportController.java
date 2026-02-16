package io.holocron.ui;

import io.holocron.report.StatsService;
import io.holocron.team.Team;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;

@Path("/reports")
@Authenticated
public class ReportController {

    @Inject
    StatsService statsService;

    @Inject
    @Location("reports/daily-rollup.html")
    Template dailyRollupTemplate;

    @GET
    @Path("/daily/{teamId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance dailyRollup(@PathParam("teamId") Long teamId) {
        Team team = Team.findById(teamId);
        if (team == null) {
            throw new NotFoundException("Team not found");
        }

        StatsService.DailyRollup rollup = statsService.getDailyRollup(team, LocalDate.now());

        return dailyRollupTemplate.data("rollup", rollup);
    }
}
