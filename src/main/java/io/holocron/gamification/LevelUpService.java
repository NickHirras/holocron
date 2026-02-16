package io.holocron.gamification;

import io.holocron.audit.AuditEntry;
import io.holocron.user.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class LevelUpService {

    private static final long KNIGHT_THRESHOLD = 1000;
    private static final long MASTER_THRESHOLD = 5000;

    @Transactional
    public void checkForLevelUp(User user, long previousXp, long currentXp) {
        String oldRank = getRankTitle(previousXp);
        String newRank = getRankTitle(currentXp);

        if (!oldRank.equals(newRank)) {
            processLevelUp(user, oldRank, newRank);
        }
    }

    private String getRankTitle(long xp) {
        if (xp >= MASTER_THRESHOLD)
            return "Master";
        if (xp >= KNIGHT_THRESHOLD)
            return "Knight";
        return "Padawan";
    }

    private void processLevelUp(User user, String oldRank, String newRank) {
        // Log the event
        AuditEntry.log(user.email, "LEVEL_UP",
                String.format("Promoted from %s to %s!", oldRank, newRank));

        // In the future, we might trigger a notification or unlock features here
    }
}
