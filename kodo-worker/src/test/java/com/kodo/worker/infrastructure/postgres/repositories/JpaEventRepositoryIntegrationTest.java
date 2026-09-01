package com.kodo.worker.infrastructure.postgres.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class JpaEventRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private JpaEventRepository repository;

    @Test
    void shouldIgnoreDuplicateEvent() {
        UUID eventId = UUID.randomUUID();

        int firstInsert = repository.insertIfAbsent(
                UUID.randomUUID(),
                eventId,
                "test-game",
                "player-123",
                "PLAYER_DIED",
                Instant.parse("2026-09-01T08:00:00Z"),
                Instant.now(),
                "{\"damage\":100}"
        );

        int secondInsert = repository.insertIfAbsent(
                UUID.randomUUID(),
                eventId,
                "test-game",
                "player-123",
                "PLAYER_DIED",
                Instant.parse("2026-09-01T08:00:00Z"),
                Instant.now(),
                "{\"damage\":100}"
        );

        assertThat(firstInsert).isEqualTo(1);
        assertThat(secondInsert).isEqualTo(0);
        assertThat(repository.count()).isEqualTo(1);
    }
}
