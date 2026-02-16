package io.holocron.audit;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import org.jboss.logging.Logger;

@Provider
@Priority(Priorities.USER)
public class AuditInterceptor implements ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(AuditInterceptor.class);

    @Inject
    SecurityIdentity identity;

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        try {
            Method method = resourceInfo.getResourceMethod();
            if (method == null)
                return;

            Audited annotation = method.getAnnotation(Audited.class);
            if (annotation == null) {
                annotation = resourceInfo.getResourceClass().getAnnotation(Audited.class);
            }

            if (annotation == null) {
                return;
            }

            String action = annotation.action();
            if (action.isEmpty()) {
                action = method.getName();
            }

            String who = "anonymous";
            if (identity != null && !identity.isAnonymous()) {
                who = identity.getPrincipal().getName();
            }

            String targetInfo = "Method: " + method.getName() + " URI: " + requestContext.getUriInfo().getPath();

            final String finalWho = who;
            final String finalAction = action;
            final String finalTarget = targetInfo;

            // Execute in a new transaction to ensure audit persistence independent of
            // request transaction
            QuarkusTransaction.requiringNew().run(() -> {
                AuditEntry entry = new AuditEntry();
                entry.who = finalWho;
                entry.action = finalAction;
                entry.targetInfo = finalTarget;
                entry.timestamp = java.time.LocalDateTime.now();
                entry.persist();
            });

        } catch (Exception e) {
            LOG.error("Failed to record audit entry", e);
        }
    }
}
