package io.holocron.dev;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class DevModeProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "holocron.auth.dev-mode", "true",
                "quarkus.oidc.enabled", "false");
    }
}
