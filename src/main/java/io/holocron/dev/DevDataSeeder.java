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
import io.holocron.ceremony.CeremonyResponse;
import io.holocron.ceremony.CeremonyAnswer;
import io.holocron.team.Team;
import io.holocron.team.TeamMember;
import io.holocron.user.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import jakarta.inject.Inject;

@ApplicationScoped
public class DevDataSeeder {

    @ConfigProperty(name = "holocron.auth.dev-mode", defaultValue = "false")
    boolean devMode;

    @Inject
    io.holocron.archive.ArtifactService artifactService;

    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        if (!devMode) {
            return;
        }

        Log.info("🔮 Holocron Dev Mode Active: Checking for seed data...");

        if (User.count() == 0) {
            Log.info("🌱 Seeding realistic Holocron data...");
            seedData();
            Log.info("✅ Seeding complete.");
        } else {
            Log.info("✨ Database already populated.");
        }
    }

    private void seedData() {
        // --- 1. Teams (Projects) ---
        Team deathStarCore = new Team();
        deathStarCore.name = "Death Star Core Architecture";
        deathStarCore.timezoneId = "America/New_York";
        deathStarCore.persist();

        Team tieNav = new Team();
        tieNav.name = "TIE Fighter Navigation Systems";
        tieNav.timezoneId = "Europe/London";
        tieNav.persist();

        Team holonetUI = new Team();
        holonetUI.name = "Imperial Holonet UI";
        holonetUI.timezoneId = "America/Los_Angeles";
        holonetUI.persist();

        // --- 2. Users with Job Titles ---
        User tarkin = createUser("tarkin@empire.gov", "Grand Moff Tarkin", "VP of Engineering", "admin");
        User vader = createUser("vader@empire.gov", "Darth Vader", "Principal System Architect", "admin");
        User veers = createUser("veers@empire.gov", "General Veers", "Engineering Manager", "user");
        User piett = createUser("piett@empire.gov", "Admiral Piett", "Senior DevOps Engineer", "user");
        User jerjerrod = createUser("jerjerrod@empire.gov", "Moff Jerjerrod", "Product Lead", "user");
        User tk421 = createUser("tk421@empire.gov", "TK-421", "QA Engineer", "user");
        User ozzel = createUser("ozzel@empire.gov", "Admiral Ozzel", "Legacy Code Maintainer", "user");
        User boba = createUser("boba@hunters.guild", "Boba Fett", "Security Consultant", "user");

        // --- 3. Memberships ---
        // Death Star Core
        joinTeam(tarkin, deathStarCore, "Sponsor");
        joinTeam(vader, deathStarCore, "Lead");
        joinTeam(veers, deathStarCore, "Member");
        joinTeam(piett, deathStarCore, "Member");

        // TIE Nav
        joinTeam(veers, tieNav, "Lead");
        joinTeam(piett, tieNav, "Member");
        joinTeam(tk421, tieNav, "QA");
        joinTeam(ozzel, tieNav, "Member");

        // Holonet UI
        joinTeam(jerjerrod, holonetUI, "Lead");
        joinTeam(tk421, holonetUI, "QA");
        joinTeam(boba, holonetUI, "Security Audit"); // External

        // --- 4. Pulses ---
        Ceremony dsPulse = createAhHocPulse(deathStarCore, "Reactor Core Weekly Sync",
                "Status check on the ultimate power in the universe.");
        Ceremony tiePulse = createAhHocPulse(tieNav, "Nav Systems Standup",
                "Updates on guidance logic and collision avoidance.");
        Ceremony holoPulse = createAhHocPulse(holonetUI, "UI/UX Sync", "Reviewing new propaganda widgets.");

        // --- 5. Historical Data ---
        seedHistory(dsPulse, List.of(vader, veers, piett));
        seedHistory(tiePulse, List.of(veers, piett, tk421, ozzel));
        seedHistory(holoPulse, List.of(jerjerrod, tk421, boba));
    }

    private User createUser(String email, String name, String title, String role) {
        User u = new User();
        u.email = email;
        u.name = name;
        u.jobTitle = title;
        u.role = role;
        u.persist();
        return u;
    }

    private void joinTeam(User user, Team team, String role) {
        TeamMember tm = new TeamMember();
        tm.user = user;
        tm.team = team;
        tm.role = role;
        tm.persist();
    }

    private Ceremony createAhHocPulse(Team team, String title, String description) {
        Ceremony c = new Ceremony();
        c.title = title;
        c.description = description;
        c.team = team;
        c.scheduleType = "WEEKLY";
        c.type = CeremonyType.PULSE;
        c.isActive = true;
        c.persist();

        // Standard Questions
        CeremonyQuestion q1 = new CeremonyQuestion();
        q1.ceremony = c;
        q1.sequence = 1;
        q1.text = "System Status Check (1-5)";
        q1.type = "SCALE";
        q1.isRequired = true;
        q1.persist();

        CeremonyQuestion q2 = new CeremonyQuestion();
        q2.ceremony = c;
        q2.sequence = 2;
        q2.text = "Blockers / Anomalies detected?";
        q2.type = "TEXT";
        q2.isRequired = false;
        q2.persist();

        return c;
    }

    private void seedHistory(Ceremony ceremony, List<User> teamMembers) {
        Random rand = new Random();
        LocalDate today = LocalDate.now();

        // Generate data for the last 12 weeks (FRIDAYS)
        for (int i = 0; i < 12; i++) {
            LocalDate date = today.minusWeeks(i);
            // Adjust to previous Friday if not Friday
            while (date.getDayOfWeek() != java.time.DayOfWeek.FRIDAY) {
                date = date.minusDays(1);
            }

            if (date.isAfter(today))
                continue; // shouldn't happen but sanity check

            // For each member, random chance to submit
            for (User u : teamMembers) {
                if (rand.nextDouble() > 0.1) { // 90% submission rate
                    createResponse(ceremony, u, date, rand);
                }
            }

            // Generate Artifact for this historical week
            // Period is typically Mon-Fri for a weekly pulse
            LocalDate periodStart = date.minusDays(4);
            artifactService.generateArtifact(ceremony, periodStart, date);
        }
    }

    private void createResponse(Ceremony ceremony, User user, LocalDate date, Random rand) {
        CeremonyResponse r = new CeremonyResponse();
        r.ceremony = ceremony;
        r.user = user;
        r.date = date;
        r.submittedAt = date.atTime(9 + rand.nextInt(8), rand.nextInt(60));
        r.persist();

        List<CeremonyQuestion> questions = CeremonyQuestion.list("ceremony", ceremony);

        // Q1: Scale
        CeremonyAnswer a1 = new CeremonyAnswer();
        a1.response = r;
        a1.question = questions.get(0);
        a1.answerValue = String.valueOf(rand.nextInt(3) + 3); // Skew towards 3-5
        a1.persist();

        // Q2: Text (30% chance of comment)
        if (rand.nextDouble() < 0.3) {
            CeremonyAnswer a2 = new CeremonyAnswer();
            a2.response = r;
            a2.question = questions.get(1);
            a2.answerValue = getRandomComment(rand);
            a2.persist();
        }
    }

    private String getRandomComment(Random rand) {
        String[] comments = {
                "All systems operational.",
                "Minor turbulence in the API layer.",
                "Waiting on parts from Alderaan.",
                "Thermal exhaust port needs shielding review.",
                "Rebels detected in sector 7.",
                "Performance within acceptable parameters.",
                "Need more coffee.",
                "Code review pending for 3 days.",
                "Deploying to staging.",
                "Fixing unit tests."
        };
        return comments[rand.nextInt(comments.length)];
    }
}
