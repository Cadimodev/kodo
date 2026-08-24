package com.kodo.contracts.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record GameEvent(
        UUID eventId,
        String gameId,
        String playerId,
        String type,
        Instant occurredAt,
        Map<String, Object> payload
) {
}