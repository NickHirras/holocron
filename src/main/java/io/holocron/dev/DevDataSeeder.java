package io.holocron.dev;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.logging.Log;

@ApplicationScoped
public class DevDataSeeder {

    @ConfigProperty(name = "holocron.auth.dev-mode", defaultValue = "false")
    boolean devMode;

    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        if (!devMode) {
            return;
        }

        Log.info("🔮 Holocron Dev Mode Active: Checking for seed data...");

        if (io.holocron.user.User.count() == 0) {
            Log.info("🌱 Seeding default users and teams...");

            io.holocron.team.Team engineering = new io.holocron.team.Team();
            engineering.name = "Engineering";
            engineering.timezoneId = "America/New_York";
            engineering.persist();

            io.holocron.user.User alice = new io.holocron.user.User();
            alice.email = "alice@holocron.io";
            alice.name = "Alice Engineer";
            alice.role = "admin";
            alice.persist();

            io.holocron.user.User bob = new io.holocron.user.User();
            bob.email = "bob@holocron.io";
            bob.name = "Bob Manager";
            bob.role = "user";
            bob.persist();

            Log.info("✅ Seeding complete: Created 1 team and 2 users.");
        } else {
            Log.info("✨ Database already populated.");
        }
    }
}
