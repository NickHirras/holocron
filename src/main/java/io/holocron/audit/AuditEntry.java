package io.holocron.audit;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_entries")
public class AuditEntry extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String who;

    @Column(nullable = false)
    public String action;

    @Column(length = 1024)
    public String targetInfo;

    @Column(nullable = false)
    public LocalDateTime timestamp;

    public static void log(String who, String action, String targetInfo) {
        AuditEntry entry = new AuditEntry();
        entry.who = who;
        entry.action = action;
        entry.targetInfo = targetInfo;
        entry.timestamp = LocalDateTime.now();
        entry.persist();
    }
}
