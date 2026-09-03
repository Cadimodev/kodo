package com.kodo.worker.application.services;

import com.kodo.contracts.events.GameEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false"
})
class EventProcessingServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private EventProcessingService eventProcessingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM outbox_events");
        jdbcTemplate.update("DELETE FROM events");
    }

    @Test
    void shouldPersistEventAndOutboxEntry() {
        GameEvent event = createEvent();

        eventProcessingService.process(event);

        assertThat(countEvents()).isEqualTo(1);
        assertThat(countOutboxEvents()).isEqualTo(1);
    }

    @Test
    void shouldNotCreateAnotherOutboxEntryForDuplicateEvent() {
        GameEvent event = createEvent();

        eventProcessingService.process(event);
        eventProcessingService.process(event);

        assertThat(countEvents()).isEqualTo(1);
        assertThat(countOutboxEvents()).isEqualTo(1);
    }

    @Test
    void shouldRollbackEventWhenOutboxInsertFails() {
        GameEvent event = createEvent();

        insertConflictingOutboxEntry(event);

        assertThatThrownBy(() -> eventProcessingService.process(event))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(countEvents()).isZero();
        assertThat(countOutboxEvents()).isEqualTo(1);
    }

    private GameEvent createEvent() {
        return new GameEvent(
                UUID.randomUUID(),
                "test-game",
                "player-123",
                "PLAYER_DIED",
                Instant.parse("2026-09-03T10:00:00Z"),
                Map.of("damage", 100)
        );
    }

    private void insertConflictingOutboxEntry(GameEvent event) {
        jdbcTemplate.update(
                """
                INSERT INTO outbox_events (
                    id,
                    event_id,
                    game_id,
                    event_type,
                    created_at,
                    published_at
                )
                VALUES (?, ?, ?, ?, ?, NULL)
                """,
                UUID.randomUUID(),
                event.eventId(),
                event.gameId(),
                event.type(),
                Timestamp.from(Instant.now())
        );
    }

    private int countEvents() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events",
                Integer.class
        );
    }

    private int countOutboxEvents() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events",
                Integer.class
        );
    }
}