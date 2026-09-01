package com.kodo.worker.infrastructure.postgres;

import com.kodo.contracts.events.GameEvent;
import com.kodo.worker.application.ports.out.EventRepository;
import com.kodo.worker.infrastructure.postgres.repositories.JpaEventRepository;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Repository
public class PostgresEventRepository implements EventRepository {

    private final JpaEventRepository jpaEventRepository;
    private final ObjectMapper objectMapper;

    public PostgresEventRepository(JpaEventRepository jpaEventRepository,
                                   ObjectMapper objectMapper) {
        this.jpaEventRepository = jpaEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(GameEvent event) {

        Map<String, Object> payload = event.payload() != null ? event.payload() : Map.of();
        String payloadJson = objectMapper.writeValueAsString(payload);

        jpaEventRepository.insertIfAbsent(
                UUID.randomUUID(),
                event.eventId(),
                event.gameId(),
                event.playerId(),
                event.type(),
                event.occurredAt(),
                Instant.now(),
                payloadJson
        );
    }
}