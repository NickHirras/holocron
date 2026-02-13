package io.holocron.ceremony;

import io.holocron.team.Team;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CeremonyRepository implements PanacheRepository<Ceremony> {

    public List<Ceremony> findByTeam(Team team) {
        return list("team", team);
    }
}
