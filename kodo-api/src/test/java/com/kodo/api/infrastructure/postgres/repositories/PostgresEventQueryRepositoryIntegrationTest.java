package com.kodo.api.infrastructure.postgres.repositories;

import com.kodo.api.application.dto.EventQuery;
import com.kodo.api.application.dto.EventResponse;
import com.kodo.api.application.dto.PagedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(PostgresEventQueryRepository.class)
class PostgresEventQueryRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void configurePostgres(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );
        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }

    @Autowired
    private PostgresEventQueryRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM events");
    }

    @Test
    void shouldFilterEventsByGamePlayerAndType() {

        UUID expectedEventId = UUID.randomUUID();

        insertEvent(
                UUID.randomUUID(),
                expectedEventId,
                "game-1",
                "player-1",
                "PLAYER_DIED",
                Instant.parse("2026-09-02T10:00:00Z")
        );

        insertEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "game-1",
                "player-1",
                "PLAYER_MOVED",
                Instant.parse("2026-09-02T10:01:00Z")
        );

        insertEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "game-1",
                "player-2",
                "PLAYER_DIED",
                Instant.parse("2026-09-02T10:02:00Z")
        );

        insertEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "game-2",
                "player-1",
                "PLAYER_DIED",
                Instant.parse("2026-09-02T10:03:00Z")
        );

        EventQuery query = new EventQuery(
                "game-1",
                "player-1",
                "PLAYER_DIED",
                0,
                20
        );

        PagedResult<EventResponse> result =
                repository.findEvents(query);

        assertEquals(1, result.items().size());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());

        EventResponse event = result.items().getFirst();

        assertEquals(expectedEventId, event.eventId());
        assertEquals("game-1", event.gameId());
        assertEquals("player-1", event.playerId());
        assertEquals("PLAYER_DIED", event.type());
    }

    @Test
    void shouldPaginateEventsOrderedByOccurredAtDescending() {

        UUID event1 = UUID.randomUUID();
        UUID event2 = UUID.randomUUID();
        UUID event3 = UUID.randomUUID();
        UUID event4 = UUID.randomUUID();
        UUID event5 = UUID.randomUUID();

        insertEvent(
                UUID.randomUUID(),
                event1,
                "game-1",
                "player-1",
                "PLAYER_MOVED",
                Instant.parse("2026-09-02T10:00:00Z")
        );

        insertEvent(
                UUID.randomUUID(),
                event2,
                "game-1",
                "player-1",
                "PLAYER_MOVED",
                Instant.parse("2026-09-02T10:01:00Z")
        );

        insertEvent(
                UUID.randomUUID(),
                event3,
                "game-1",
                "player-1",
                "PLAYER_MOVED",
                Instant.parse("2026-09-02T10:02:00Z")
        );

        insertEvent(
                UUID.randomUUID(),
                event4,
                "game-1",
                "player-1",
                "PLAYER_MOVED",
                Instant.parse("2026-09-02T10:03:00Z")
        );

        insertEvent(
                UUID.randomUUID(),
                event5,
                "game-1",
                "player-1",
                "PLAYER_MOVED",
                Instant.parse("2026-09-02T10:04:00Z")
        );

        EventQuery query = new EventQuery(
                "game-1",
                null,
                null,
                1,
                2
        );

        PagedResult<EventResponse> result =
                repository.findEvents(query);

        assertEquals(2, result.items().size());
        assertEquals(1, result.page());
        assertEquals(2, result.size());
        assertEquals(5, result.totalElements());
        assertEquals(3, result.totalPages());

        assertEquals(event3, result.items().get(0).eventId());
        assertEquals(event2, result.items().get(1).eventId());
    }

    private void insertEvent(
            UUID id,
            UUID eventId,
            String gameId,
            String playerId,
            String eventType,
            Instant occurredAt
    ) {

        jdbcTemplate.update(
                """
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
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    CAST(? AS jsonb)
                )
                """,
                id,
                eventId,
                gameId,
                playerId,
                eventType,
                Timestamp.from(occurredAt),
                Timestamp.from(occurredAt.plusSeconds(1)),
                "{\"source\":\"integration-test\"}"
        );
    }
}