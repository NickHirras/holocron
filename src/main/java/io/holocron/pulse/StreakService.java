package io.holocron.pulse;

import io.holocron.audit.AuditEntry;
import io.holocron.user.User;
import io.holocron.user.UserStats;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;

@ApplicationScoped
public class StreakService {

    @Transactional
    public void incrementStreak(User user) {
        UserStats stats = UserStats.findByUser(user);
        if (stats == null) {
            // Should be handled by migration, but just in case
            stats = new UserStats();
            stats.user = user;
            stats.persist();
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        if (stats.lastPulseDate != null && stats.lastPulseDate.isEqual(today)) {
            // Already pulsed today, do nothing
            return;
        }

        if (stats.lastPulseDate != null && stats.lastPulseDate.isEqual(yesterday)) {
            // Consecutive day
            stats.currentStreak++;
        } else {
            // Streak broken or new
            stats.currentStreak = 1;
        }

        if (stats.currentStreak > stats.longestStreak) {
            stats.longestStreak = stats.currentStreak;
        }

        stats.lastPulseDate = today;
        stats.persist();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void incrementXp(User user, long amount, String reason) {
        if (amount <= 0)
            return;

        UserStats stats = UserStats.findByUser(user);
        if (stats == null) {
            stats = new UserStats();
            stats.user = user;
            stats.persist();
        }

        stats.totalXp += amount;
        stats.persist();

        AuditEntry.log(user.email, "XP_GAIN",
                String.format("Gained %d XP. Reason: %s. Total: %d", amount, reason, stats.totalXp));
    }
}
