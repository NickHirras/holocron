package io.holocron.ceremony;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CeremonyQuestionRepository implements PanacheRepository<CeremonyQuestion> {

    public List<CeremonyQuestion> findByCeremony(Ceremony ceremony) {
        return list("ceremony = ?1 ORDER BY sequence ASC", ceremony);
    }
}
