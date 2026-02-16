package io.holocron.pulse;

import io.holocron.audit.AuditEntry;
import io.holocron.gamification.LevelUpService;
import io.holocron.user.User;
import io.holocron.user.UserStats;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class StreakService {

    @Inject
    LevelUpService levelUpService;

    @Transactional
    public void incrementStreak(User user) {
        incrementStreak(user, LocalDate.now());
    }

    @Transactional
    public void incrementStreak(User user, LocalDate today) {
        UserStats stats = UserStats.findByUser(user);
        if (stats == null) {
            // Should be handled by migration, but just in case
            stats = new UserStats();
            stats.user = user;
            stats.persist();
        }

        if (stats.lastPulseDate != null && stats.lastPulseDate.isEqual(today)) {
            // Already pulsed today, do nothing
            return;
        }

        if (isStreakAlive(stats.lastPulseDate, today)) {
            // Streak continues
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

    private boolean isStreakAlive(LocalDate lastPulseDate, LocalDate today) {
        if (lastPulseDate == null) {
            return false;
        }

        long daysBetween = ChronoUnit.DAYS.between(lastPulseDate, today);

        // If it was yesterday, streak is alive
        if (daysBetween == 1) {
            return true;
        }

        // If it was more than yesterday, check if the gap was covered by weekends
        // e.g. Friday -> Monday (Gap: Sat, Sun)
        // e.g. Friday -> Sunday (Gap: Sat)
        if (daysBetween > 1) {
            LocalDate checkDate = lastPulseDate.plusDays(1);
            while (checkDate.isBefore(today)) {
                if (!isWeekend(checkDate)) {
                    return false; // Found a weekday in the gap, streak broken
                }
                checkDate = checkDate.plusDays(1);
            }
            return true; // All gap days were weekends
        }

        return false;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    @Transactional
    public void incrementXp(User user, long amount, String reason) {
        if (amount <= 0)
            return;

        UserStats stats = UserStats.findByUser(user);
        if (stats == null) {
            stats = new UserStats();
            stats.user = user;
            stats.persist();
        }

        long previousXp = stats.totalXp;
        stats.totalXp += amount;
        stats.persist();

        // Check for level up
        levelUpService.checkForLevelUp(user, previousXp, stats.totalXp);

        AuditEntry.log(user.email, "XP_GAIN",
                String.format("Gained %d XP. Reason: %s. Total: %d", amount, reason, stats.totalXp));
    }
}
