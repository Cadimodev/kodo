package com.kodo.worker.infrastructure.postgres;

import com.kodo.contracts.events.GameEvent;
import com.kodo.worker.application.ports.out.EventRepository;
import com.kodo.worker.infrastructure.postgres.entities.EventEntity;
import com.kodo.worker.infrastructure.postgres.repositories.JpaEventRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Repository
public class PostgresEventRepository implements EventRepository {

    private final JpaEventRepository jpaEventRepository;

    public PostgresEventRepository(JpaEventRepository jpaEventRepository) {
        this.jpaEventRepository = jpaEventRepository;
    }

    @Override
    public void save(GameEvent event) {
        Map<String, Object> payload = event.payload() != null ? event.payload() : Map.of();
        EventEntity entity = new EventEntity(
                UUID.randomUUID(),
                event.eventId(),
                event.gameId(),
                event.playerId(),
                event.type(),
                event.occurredAt(),
                Instant.now(),
                payload
        );

        jpaEventRepository.save(entity);
    }
}