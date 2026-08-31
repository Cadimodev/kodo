package com.kodo.worker.infrastructure.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "game_id", nullable = false, length = 100)
    private String gameId;

    @Column(name = "player_id", length = 100)
    private String playerId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    protected EventEntity() {
    }

    public EventEntity(
            UUID id,
            UUID eventId,
            String gameId,
            String playerId,
            String eventType,
            Instant occurredAt,
            Instant receivedAt,
            Map<String, Object> payload) {

        this.id = id;
        this.eventId = eventId;
        this.gameId = gameId;
        this.playerId = playerId;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.receivedAt = receivedAt;
        this.payload = payload;
    }
}
