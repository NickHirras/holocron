package io.holocron.pulse;

import io.holocron.user.User;
import io.holocron.user.UserStats;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class StreakServiceTest {

    @Inject
    StreakService streakService;

    @Inject
    jakarta.persistence.EntityManager em;

    User user;

    @BeforeEach
    @Transactional
    void setup() {
        UserStats.deleteAll();
        User.deleteAll();

        user = new User();
        user.email = "test@example.com";
        user.name = "Test User";
        user.persist();
    }

    @Test
    @Transactional
    void testIncrementStreak() {
        // Initial Streak
        streakService.incrementStreak(user);
        em.flush();
        em.clear();

        // Re-fetch user after clear
        user = User.findById(user.id);

        UserStats stats = UserStats.findByUser(user);
        assertNotNull(stats);
        assertEquals(1, stats.currentStreak);
        assertEquals(1, stats.longestStreak);
        assertEquals(LocalDate.now(), stats.lastPulseDate);

        // Same day submission - should not increment
        streakService.incrementStreak(user);
        em.flush();
        em.clear();
        user = User.findById(user.id);

        stats = UserStats.findByUser(user);
        assertEquals(1, stats.currentStreak);

        // Simulate yesterday
        stats.lastPulseDate = LocalDate.now().minusDays(1);
        stats.persist();
        em.flush();
        em.clear();
        user = User.findById(user.id);

        // Increment for "today" (relative to simulation)
        streakService.incrementStreak(user);
        em.flush();
        em.clear();
        user = User.findById(user.id);

        stats = UserStats.findByUser(user);
        assertEquals(2, stats.currentStreak);
        assertEquals(2, stats.longestStreak);

        // Simulate gap (2 days ago)
        stats.lastPulseDate = LocalDate.now().minusDays(2);
        stats.persist();
        em.flush();
        em.clear();
        user = User.findById(user.id);

        // Should reset
        streakService.incrementStreak(user);
        em.flush();
        em.clear();
        user = User.findById(user.id);

        stats = UserStats.findByUser(user);
        assertEquals(1, stats.currentStreak);
        assertEquals(2, stats.longestStreak); // Longest preserved
    }

    @Test
    void testIncrementXp() {
        streakService.incrementXp(user, 100, "Test");

        // Since incrementXp is REQUIRES_NEW, we might need to handle transaction
        // visibility
        // But for verifying DB state, a fresh fetch should work.
        // We'll trust the separate transaction committed.

        UserStats stats = UserStats.findByUser(user);
        assertNotNull(stats);
        assertEquals(100, stats.totalXp);

        // Clear L1 cache to ensure we don't get the old 'stats' instance if a session
        // is open
        em.clear();
        user = User.findById(user.id); // Re-fetch user

        streakService.incrementXp(user, 50, "Test 2");

        em.clear(); // Clear again just to be safe
        user = User.findById(user.id);

        stats = UserStats.findByUser(user);
        assertEquals(150, stats.totalXp);
    }
}
