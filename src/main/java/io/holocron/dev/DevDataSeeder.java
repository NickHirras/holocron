package io.holocron.dev;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.logging.Log;
import io.holocron.ceremony.Ceremony;
import io.holocron.ceremony.CeremonyType;
import io.holocron.ceremony.CeremonyQuestion;
import io.holocron.team.Team;

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

            Team engineering = new Team();
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

        // Ensure Pulse exists
        seedPulse();
    }

    private void seedPulse() {
        Team engineering = Team.find("name", "Engineering").firstResult();
        if (engineering != null) {
            if (Ceremony.find("type = ?1 and team = ?2", CeremonyType.PULSE, engineering).count() == 0) {
                Log.info("💓 Seeding Weekly Pulse...");
                Ceremony pulse = new Ceremony();
                pulse.title = "Weekly Pulse";
                pulse.description = "Weekly team health check";
                pulse.team = engineering;
                pulse.scheduleType = "WEEKLY";
                pulse.type = CeremonyType.PULSE;
                pulse.isActive = true;
                pulse.persist();

                // Questions
                CeremonyQuestion q1 = new CeremonyQuestion();
                q1.ceremony = pulse;
                q1.sequence = 1;
                q1.text = "How are you feeling this week?";
                q1.type = "SCALE"; // 1-5
                q1.isRequired = true;
                q1.persist();

                CeremonyQuestion q2 = new CeremonyQuestion();
                q2.ceremony = pulse;
                q2.sequence = 2;
                q2.text = "Any blockers or concerns?";
                q2.type = "TEXT";
                q2.isRequired = false;
                q2.persist();

                Log.info("✅ Seeding Pulse complete.");
            }
        }
    }
}
