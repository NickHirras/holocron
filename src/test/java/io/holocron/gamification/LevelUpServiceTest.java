package io.holocron.gamification;

import io.holocron.user.User;
import io.holocron.user.UserStats;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.holocron.audit.AuditEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class LevelUpServiceTest {

    @Inject
    LevelUpService levelUpService;

    @Inject
    jakarta.persistence.EntityManager em;

    User user;

    @BeforeEach
    @Transactional
    void setup() {
        AuditEntry.deleteAll();
        UserStats.deleteAll();
        User.deleteAll();

        user = new User();
        user.email = "test_level@example.com";
        user.name = "Test Leveler";
        user.persist();
    }

    @Test
    @Transactional
    void testNoLevelUp() {
        // Padawan -> Padawan
        levelUpService.checkForLevelUp(user, 100, 200);

        long auditCount = AuditEntry.count("action = ?1", "LEVEL_UP");
        assertEquals(0, auditCount);
    }

    @Test
    @Transactional
    void testPromotionToKnight() {
        // Padawan (990) -> Knight (1000)
        levelUpService.checkForLevelUp(user, 990, 1000);

        long auditCount = AuditEntry.count("action = ?1", "LEVEL_UP");
        assertEquals(1, auditCount);

        AuditEntry entry = AuditEntry.find("action = ?1", "LEVEL_UP").firstResult();
        assertTrue(entry.targetInfo.contains("Promoted from Padawan to Knight"));
    }

    @Test
    @Transactional
    void testPromotionToMaster() {
        // Knight (4990) -> Master (5000)
        levelUpService.checkForLevelUp(user, 4990, 5000);

        long auditCount = AuditEntry.count("action = ?1", "LEVEL_UP");
        assertEquals(1, auditCount);

        AuditEntry entry = AuditEntry.find("action = ?1", "LEVEL_UP").firstResult();
        assertTrue(entry.targetInfo.contains("Promoted from Knight to Master"));
    }
}
