package com.kodo.worker.application.dto;

import java.util.UUID;

public record PendingOutboxEvent(
        UUID id,
        UUID eventId,
        String gameId,
        String eventType
) {
}