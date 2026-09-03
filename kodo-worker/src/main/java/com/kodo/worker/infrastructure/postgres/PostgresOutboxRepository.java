package com.kodo.worker.infrastructure.postgres;

import com.kodo.contracts.events.GameEvent;
import com.kodo.worker.application.dto.PendingOutboxEvent;
import com.kodo.worker.application.ports.out.OutboxRepository;
import com.kodo.worker.infrastructure.postgres.entities.OutboxEventEntity;
import com.kodo.worker.infrastructure.postgres.repositories.JpaOutboxRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class PostgresOutboxRepository implements OutboxRepository {

    private final EntityManager entityManager;
    private final JpaOutboxRepository jpaOutboxRepository;

    public PostgresOutboxRepository(EntityManager entityManager,
                                    JpaOutboxRepository jpaOutboxRepository) {
        this.entityManager = entityManager;
        this.jpaOutboxRepository = jpaOutboxRepository;
    }

    @Override
    public void savePending(GameEvent event) {
        entityManager.persist(
                new OutboxEventEntity(
                        UUID.randomUUID(),
                        event.eventId(),
                        event.gameId(),
                        event.type(),
                        Instant.now()
                )
        );
    }

    @Override
    public List<PendingOutboxEvent> findPending(int limit) {

        return jpaOutboxRepository.findPending(limit)
                .stream()
                .map(entity -> new PendingOutboxEvent(
                        entity.getId(),
                        entity.getEventId(),
                        entity.getGameId(),
                        entity.getEventType()
                ))
                .toList();
    }

    @Override
    public void markPublished(UUID id, Instant publishedAt) {
        jpaOutboxRepository.markPublished(id, publishedAt);
    }
}