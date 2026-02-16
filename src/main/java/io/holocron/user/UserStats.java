package io.holocron.user;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import java.time.LocalDate;

@Entity
@Table(name = "user_stats")
public class UserStats extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    public User user;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    public int currentStreak;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    public int longestStreak;

    public LocalDate lastPulseDate;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    public long totalXp;

    public static UserStats findByUser(User user) {
        return find("user", user).firstResult();
    }

    public String getRankTitle() {
        if (totalXp >= 5000)
            return "Master";
        if (totalXp >= 1000)
            return "Knight";
        return "Padawan";
    }

    public int getRankProgress() {
        if (totalXp < 1000) {
            return (int) ((totalXp / 1000.0) * 100);
        } else if (totalXp < 5000) {
            return (int) (((totalXp - 1000) / 4000.0) * 100);
        } else {
            return 100;
        }
    }
}
