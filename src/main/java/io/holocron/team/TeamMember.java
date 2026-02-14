package io.holocron.team;

import io.holocron.user.User;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "team_members")
public class TeamMember extends PanacheEntity {

    @ManyToOne
    public User user;

    @ManyToOne
    public Team team;

    public String role; // e.g., "Lead", "Member", "Observer"

    public static List<TeamMember> findByUser(User user) {
        return find("user", user).list();
    }

    public static List<TeamMember> findByTeam(Team team) {
        return find("team", team).list();
    }
}
