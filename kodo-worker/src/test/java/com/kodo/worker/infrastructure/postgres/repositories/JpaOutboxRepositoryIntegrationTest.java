package com.kodo.worker.infrastructure.postgres.repositories;

import com.kodo.worker.infrastructure.postgres.entities.OutboxEventEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaOutboxRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private JpaOutboxRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldReturnPendingEventsOrderedAndLimited() {
        UUID oldestPending = UUID.randomUUID();
        UUID secondPending = UUID.randomUUID();
        UUID newestPending = UUID.randomUUID();
        UUID published = UUID.randomUUID();

        insertOutboxEvent(
                oldestPending,
                Instant.parse("2026-09-03T10:00:00Z"),
                null
        );

        insertOutboxEvent(
                secondPending,
                Instant.parse("2026-09-03T10:01:00Z"),
                null
        );

        insertOutboxEvent(
                newestPending,
                Instant.parse("2026-09-03T10:02:00Z"),
                null
        );

        insertOutboxEvent(
                published,
                Instant.parse("2026-09-03T09:00:00Z"),
                Instant.parse("2026-09-03T09:05:00Z")
        );

        List<OutboxEventEntity> result =
                repository.findPending(2);

        assertThat(result)
                .extracting(OutboxEventEntity::getId)
                .containsExactly(
                        oldestPending,
                        secondPending
                );
    }

    private void insertOutboxEvent(
            UUID id,
            Instant createdAt,
            Instant publishedAt
    ) {
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
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                id,
                UUID.randomUUID(),
                "test-game",
                "PLAYER_DIED",
                Timestamp.from(createdAt),
                publishedAt != null
                        ? Timestamp.from(publishedAt)
                        : null
        );
    }
}