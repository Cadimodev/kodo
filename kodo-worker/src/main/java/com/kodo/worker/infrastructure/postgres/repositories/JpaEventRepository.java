package com.kodo.worker.infrastructure.postgres.repositories;

import com.kodo.worker.infrastructure.postgres.entities.EventEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface JpaEventRepository extends JpaRepository<EventEntity, UUID> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO events (
                id,
                event_id,
                game_id,
                player_id,
                event_type,
                occurred_at,
                received_at,
                payload
            )
            VALUES (
                :id,
                :eventId,
                :gameId,
                :playerId,
                :eventType,
                :occurredAt,
                :receivedAt,
                CAST(:payload AS jsonb)
            )
            ON CONFLICT (game_id, event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("eventId") UUID eventId,
            @Param("gameId") String gameId,
            @Param("playerId") String playerId,
            @Param("eventType") String eventType,
            @Param("occurredAt") Instant occurredAt,
            @Param("receivedAt") Instant receivedAt,
            @Param("payload") String payload
    );
}
