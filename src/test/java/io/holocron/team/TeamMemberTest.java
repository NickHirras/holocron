package io.holocron.team;

import io.holocron.user.User;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import org.junit.jupiter.api.Test;
import java.util.List;
import jakarta.persistence.PersistenceException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class TeamMemberTest {

    @Test
    @TestTransaction
    public void testTeamMembership() {
        // Create User
        User user = new User();
        user.email = "test@example.com";
        user.name = "Test User";
        user.persist();

        // Create Teams
        Team team1 = new Team();
        team1.name = "Team Alpha";
        team1.persist();

        Team team2 = new Team();
        team2.name = "Team Beta";
        team2.persist();

        // Create Memberships
        TeamMember m1 = new TeamMember();
        m1.user = user;
        m1.team = team1;
        m1.role = "Lead";
        m1.persist();

        TeamMember m2 = new TeamMember();
        m2.user = user;
        m2.team = team2;
        m2.role = "Member";
        m2.persist();

        // Verify findByUser
        List<TeamMember> userMemberships = TeamMember.findByUser(user);
        assertEquals(2, userMemberships.size());

        // Verify findByTeam
        List<TeamMember> team1Members = TeamMember.findByTeam(team1);
        assertEquals(1, team1Members.size());
        assertEquals("test@example.com", team1Members.get(0).user.email);
    }

    @Test
    @TestTransaction
    public void testUniqueConstraint() {
        User user = new User();
        user.email = "duplicate@example.com";
        user.persist();

        Team team = new Team();
        team.name = "Duplicate Team";
        team.persist();

        TeamMember m1 = new TeamMember();
        m1.user = user;
        m1.team = team;
        m1.persist();

        TeamMember m2 = new TeamMember();
        m2.user = user;
        m2.team = team;
        assertThrows(PersistenceException.class, () -> {
            m2.persist();
            m2.flush(); // Force flush to trigger constraint violation
        });
    }
}
