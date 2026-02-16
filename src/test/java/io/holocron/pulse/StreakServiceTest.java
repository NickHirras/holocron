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
    void testWeekendProtection() {
        // Thursday (Day 1)
        LocalDate thursday = LocalDate.of(2023, 10, 26);
        streakService.incrementStreak(user, thursday);

        // Friday (Day 2)
        LocalDate friday = LocalDate.of(2023, 10, 27);
        streakService.incrementStreak(user, friday);

        UserStats stats = getStats();
        assertEquals(2, stats.currentStreak, "Streak should clearly be 2");

        // Monday (Day 3, after weekend gap which should be protected)
        LocalDate monday = LocalDate.of(2023, 10, 30);
        streakService.incrementStreak(user, monday);

        stats = getStats();
        assertEquals(3, stats.currentStreak, "Stranger, the weekend gap (Sat, Sun) should preserve the streak!");
    }

    @Test
    @Transactional
    void testWeekendProtectionPartial() {
        // Friday (Day 1)
        LocalDate friday = LocalDate.of(2023, 10, 27);
        streakService.incrementStreak(user, friday);

        // Sunday (Day 2 - Gap: Saturday)
        LocalDate sunday = LocalDate.of(2023, 10, 29);
        streakService.incrementStreak(user, sunday);

        UserStats stats = getStats();
        assertEquals(2, stats.currentStreak, "Gap of Saturday should be protected");
    }

    @Test
    @Transactional
    void testBrokenStreak() {
        // Wednesday (Day 1)
        LocalDate wed = LocalDate.of(2023, 10, 25);
        streakService.incrementStreak(user, wed);

        // Friday (Day 2 - Gap: Thursday, NOT protected)
        LocalDate fri = LocalDate.of(2023, 10, 27);
        streakService.incrementStreak(user, fri);

        UserStats stats = getStats();
        assertEquals(1, stats.currentStreak, "Gap of Thursday should break streak");
    }

    @Test
    @Transactional
    void testNormalDailyStreak() {
        LocalDate d1 = LocalDate.of(2023, 1, 1);
        streakService.incrementStreak(user, d1);
        assertEquals(1, getStats().currentStreak);

        LocalDate d2 = LocalDate.of(2023, 1, 2);
        streakService.incrementStreak(user, d2);
        assertEquals(2, getStats().currentStreak);
    }

    private UserStats getStats() {
        em.flush();
        em.clear();
        return UserStats.findByUser(user);
    }
}
