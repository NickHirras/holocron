package io.holocron.team;

import io.holocron.user.User;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;

import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "team_members", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "team_id" }))
public class TeamMember extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    public User user;

    @ManyToOne
    public Team team;

    public String role; // e.g., "Lead", "Member", "Observer"

    public static List<TeamMember> findByUser(User user) {
        return find("SELECT tm FROM TeamMember tm JOIN FETCH tm.team WHERE tm.user = ?1 ORDER BY tm.team.name ASC",
                user).list();
    }

    public static List<TeamMember> findByTeam(Team team) {
        return find("SELECT tm FROM TeamMember tm JOIN FETCH tm.user WHERE tm.team = ?1", team).list();
    }
}
