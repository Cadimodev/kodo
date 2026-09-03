package com.kodo.worker.infrastructure.postgres.repositories;

import com.kodo.worker.infrastructure.postgres.entities.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaOutboxRepository
        extends JpaRepository<OutboxEventEntity, UUID> {

    @Query(value = """
            SELECT *
            FROM outbox_events
            WHERE published_at IS NULL
            ORDER BY created_at ASC, id ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<OutboxEventEntity> findPending(
            @Param("limit") int limit
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE OutboxEventEntity o
        SET o.publishedAt = :publishedAt
        WHERE o.id = :id
          AND o.publishedAt IS NULL
        """)
    int markPublished(
            @Param("id") UUID id,
            @Param("publishedAt") Instant publishedAt
    );
}