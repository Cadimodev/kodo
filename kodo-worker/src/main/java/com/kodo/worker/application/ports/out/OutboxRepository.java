package com.kodo.worker.application.ports.out;

import com.kodo.contracts.events.GameEvent;
import com.kodo.worker.application.dto.PendingOutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {

    void savePending(GameEvent event);

    List<PendingOutboxEvent> findPending(int limit);

    void markPublished(UUID id, Instant publishedAt);
}