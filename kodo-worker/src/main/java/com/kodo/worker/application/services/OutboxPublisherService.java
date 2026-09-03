package com.kodo.worker.application.services;

import com.kodo.contracts.events.GameEventPersisted;
import com.kodo.worker.application.dto.PendingOutboxEvent;
import com.kodo.worker.application.ports.in.OutboxProcessor;
import com.kodo.worker.application.ports.out.OutboxRepository;
import com.kodo.worker.application.ports.out.PersistedEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxPublisherService implements OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final PersistedEventPublisher persistedEventPublisher;

    public OutboxPublisherService(
            OutboxRepository outboxRepository,
            PersistedEventPublisher persistedEventPublisher
    ) {
        this.outboxRepository = outboxRepository;
        this.persistedEventPublisher = persistedEventPublisher;
    }

    @Override
    public int publishPending(int batchSize) {

        List<PendingOutboxEvent> pendingEvents =
                outboxRepository.findPending(batchSize);

        int publishedCount = 0;

        for (PendingOutboxEvent pendingEvent : pendingEvents) {

            GameEventPersisted event = new GameEventPersisted(
                    pendingEvent.eventId(),
                    pendingEvent.gameId(),
                    pendingEvent.eventType()
            );

            persistedEventPublisher.publish(event);

            outboxRepository.markPublished(
                    pendingEvent.id(),
                    Instant.now()
            );

            publishedCount++;
        }

        return publishedCount;
    }
}