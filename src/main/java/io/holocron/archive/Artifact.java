package io.holocron.archive;

import io.holocron.ceremony.Ceremony;
import io.holocron.team.Team;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "artifacts", indexes = {
        @Index(name = "idx_artifact_team_period", columnList = "team_id, periodStart")
})
public class Artifact extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    public Team team;

    @ManyToOne
    public Ceremony ceremony;

    public LocalDate periodStart;

    public LocalDate periodEnd;

    @Lob
    public String summaryJson;

    public LocalDateTime createdAt;

    public static Artifact findLatest(Team team) {
        return find("team = ?1 order by periodEnd desc", team).firstResult();
    }
}
