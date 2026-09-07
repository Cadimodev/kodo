package com.kodo.worker.application.services;

import com.kodo.contracts.events.GameEventPersisted;
import com.kodo.worker.application.dto.PendingOutboxEvent;
import com.kodo.worker.application.ports.out.OutboxRepository;
import com.kodo.worker.application.ports.out.PersistedEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OutboxPublisherServiceTest {

    private final OutboxRepository outboxRepository =
            mock(OutboxRepository.class);

    private final PersistedEventPublisher persistedEventPublisher =
            mock(PersistedEventPublisher.class);

    private final OutboxPublisherService service =
            new OutboxPublisherService(
                    outboxRepository,
                    persistedEventPublisher
            );

    @Test
    void shouldPublishPendingEventsAndReturnPublishedCount() {
        PendingOutboxEvent first =
                pendingEvent("game-1", "PLAYER_DIED");
        PendingOutboxEvent second =
                pendingEvent("game-2", "MATCH_STARTED");

        when(outboxRepository.findPending(100))
                .thenReturn(List.of(first, second));

        int publishedCount = service.publishPending(100);

        assertThat(publishedCount).isEqualTo(2);

        verify(outboxRepository).findPending(100);

        verify(persistedEventPublisher).publish(
                new GameEventPersisted(
                        first.eventId(),
                        "game-1",
                        "PLAYER_DIED"
                )
        );
        verify(persistedEventPublisher).publish(
                new GameEventPersisted(
                        second.eventId(),
                        "game-2",
                        "MATCH_STARTED"
                )
        );

        verify(outboxRepository)
                .markPublished(eq(first.id()), any(Instant.class));
        verify(outboxRepository)
                .markPublished(eq(second.id()), any(Instant.class));

        verifyNoMoreInteractions(
                outboxRepository,
                persistedEventPublisher
        );
    }

    @Test
    void shouldMarkPublishedOnlyAfterPublishing() {
        PendingOutboxEvent pending =
                pendingEvent("game-1", "PLAYER_DIED");

        when(outboxRepository.findPending(100))
                .thenReturn(List.of(pending));

        service.publishPending(100);

        InOrder order = inOrder(
                outboxRepository,
                persistedEventPublisher
        );

        order.verify(outboxRepository).findPending(100);
        order.verify(persistedEventPublisher).publish(
                new GameEventPersisted(
                        pending.eventId(),
                        pending.gameId(),
                        pending.eventType()
                )
        );
        order.verify(outboxRepository)
                .markPublished(eq(pending.id()), any(Instant.class));
    }

    @Test
    void shouldNotMarkPublishedWhenPublishingFails() {
        PendingOutboxEvent pending =
                pendingEvent("game-1", "PLAYER_DIED");

        when(outboxRepository.findPending(100))
                .thenReturn(List.of(pending));

        RuntimeException failure =
                new IllegalStateException("Kafka unavailable");

        doThrow(failure)
                .when(persistedEventPublisher)
                .publish(any(GameEventPersisted.class));

        assertThatThrownBy(() -> service.publishPending(100))
                .isSameAs(failure);

        verify(outboxRepository, never())
                .markPublished(any(UUID.class), any(Instant.class));
    }

    @Test
    void shouldStopBatchWhenPublishingFails() {
        PendingOutboxEvent first =
                pendingEvent("game-1", "PLAYER_DIED");
        PendingOutboxEvent second =
                pendingEvent("game-2", "MATCH_STARTED");
        PendingOutboxEvent third =
                pendingEvent("game-3", "MATCH_FINISHED");

        when(outboxRepository.findPending(100))
                .thenReturn(List.of(first, second, third));

        GameEventPersisted firstEvent = new GameEventPersisted(
                first.eventId(),
                first.gameId(),
                first.eventType()
        );
        GameEventPersisted secondEvent = new GameEventPersisted(
                second.eventId(),
                second.gameId(),
                second.eventType()
        );

        RuntimeException failure =
                new IllegalStateException("Kafka unavailable");

        doThrow(failure)
                .when(persistedEventPublisher)
                .publish(secondEvent);

        assertThatThrownBy(() -> service.publishPending(100))
                .isSameAs(failure);

        InOrder order = inOrder(
                outboxRepository,
                persistedEventPublisher
        );

        order.verify(outboxRepository).findPending(100);
        order.verify(persistedEventPublisher).publish(firstEvent);
        order.verify(outboxRepository)
                .markPublished(eq(first.id()), any(Instant.class));
        order.verify(persistedEventPublisher).publish(secondEvent);

        verify(outboxRepository, never())
                .markPublished(eq(second.id()), any(Instant.class));
        verify(outboxRepository, never())
                .markPublished(eq(third.id()), any(Instant.class));

        verify(persistedEventPublisher, never()).publish(
                new GameEventPersisted(
                        third.eventId(),
                        third.gameId(),
                        third.eventType()
                )
        );

        verifyNoMoreInteractions(
                outboxRepository,
                persistedEventPublisher
        );
    }

    private PendingOutboxEvent pendingEvent(
            String gameId,
            String eventType
    ) {
        return new PendingOutboxEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                gameId,
                eventType
        );
    }
}