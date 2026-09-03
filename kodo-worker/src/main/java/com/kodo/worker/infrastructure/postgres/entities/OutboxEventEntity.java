package com.kodo.worker.infrastructure.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxEventEntity() {
    }

    public OutboxEventEntity(
            UUID id,
            UUID eventId,
            String gameId,
            String eventType,
            Instant createdAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.gameId = gameId;
        this.eventType = eventType;
        this.createdAt = createdAt;
        this.publishedAt = null;
    }
}