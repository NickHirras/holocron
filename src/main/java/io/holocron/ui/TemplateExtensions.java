import io.quarkus.qute.TemplateGlobal;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;

@ApplicationScoped
public class TemplateExtensions {

    @TemplateGlobal(name = "identity")
    public static SecurityIdentity identity() {
        return CDI.current().select(SecurityIdentity.class).get();
    }
}
