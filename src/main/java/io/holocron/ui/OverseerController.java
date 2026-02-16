package io.holocron.ui;

import io.holocron.report.OverseerService;
import io.holocron.user.User;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/overseer")
@Authenticated
public class OverseerController {

    @Inject
    Template overseer;

    @Inject
    SecurityIdentity identity;

    @Inject
    OverseerService overseerService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        String email = identity.getPrincipal().getName();
        User user = User.findByEmail(email);

        if (user == null) {
            // If user doesn't exist in DB, they can't be a Lead of any team.
            // We can return an empty dashboard or redirect.
            // For dev/mocking, if we are in a state where user is not found,
            // the service will just return empty list of teams, which is fine.
            user = new User();
            user.email = email;
            user.name = email;
        }

        OverseerService.OverseerDashboardDTO dashboardData = overseerService.getDashboardData(user);

        return overseer.data("dashboard", dashboardData)
                .data("user", user);
    }

    @Inject
    @io.quarkus.qute.Location("overseer-details")
    Template detailsTemplate;

    @GET
    @Path("/operative/{userId}/details")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance details(@jakarta.ws.rs.PathParam("userId") Long userId) {
        String email = identity.getPrincipal().getName();
        User lead = User.findByEmail(email);
        if (lead == null) {
            lead = new User();
            lead.email = email;
        }

        OverseerService.OperativeDetailsDTO details = overseerService.getOperativeDetails(lead, userId);
        return detailsTemplate.data("details", details);
    }
}
