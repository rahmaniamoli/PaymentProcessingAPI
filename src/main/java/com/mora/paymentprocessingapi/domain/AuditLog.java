package com.mora.paymentprocessingapi.domain;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_audit_logs_event_type", columnList = "event_type")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "actor", length = 254)
    private String actor;

    @Column(name = "outcome", nullable = false, length = 30)
    private String outcome;

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditLog() {
    }

    public AuditLog(
            String eventType,
            String actor,
            String outcome,
            String correlationId
    ) {
        this.eventType = eventType;
        this.actor = actor;
        this.outcome = outcome;
        this.correlationId = correlationId;
        this.occurredAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getActor() { return actor; }
    public String getOutcome() { return outcome; }
    public String getCorrelationId() { return correlationId; }
    public Instant getOccurredAt() { return occurredAt; }
}
