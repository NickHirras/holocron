package io.holocron.ceremony;

import io.holocron.team.Team;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ceremonies")
public class Ceremony extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String title;

    public String description;

    @ManyToOne
    public Team team;

    // stored as string for now, e.g., "DAILY", "WEEKLY"
    public String scheduleType;

    public boolean isActive;
}
