package com.kodo.worker.infrastructure.postgres;

import com.kodo.contracts.events.GameEvent;
import com.kodo.worker.application.ports.out.OutboxRepository;
import com.kodo.worker.infrastructure.postgres.entities.OutboxEventEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class PostgresOutboxRepository implements OutboxRepository {

    private final EntityManager entityManager;

    public PostgresOutboxRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
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
}