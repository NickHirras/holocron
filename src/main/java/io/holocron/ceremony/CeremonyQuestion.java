package io.holocron.ceremony;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ceremony_questions")
public class CeremonyQuestion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    public Ceremony ceremony;

    public String text;

    // TEXT, SCALE, BOOLEAN, SELECTION
    public String type;

    public Integer sequence;

    public boolean isRequired;
}
