package io.holocron.util;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Provider
@PreMatching
public class RequestLoggingFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        String log = "Request: " + context.getMethod() + " " + context.getUriInfo().getRequestUri() + "\n";
        log += "Headers: " + context.getHeaders() + "\n";
        if (context.hasEntity()) {
            log += "Has Entity: true\n";
            log += "Media Type: " + context.getMediaType() + "\n";
        } else {
            log += "Has Entity: false\n";
        }
        log += "--------------------------------------------------\n";
        try {
            Files.writeString(Path.of("request_log.txt"), log, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
