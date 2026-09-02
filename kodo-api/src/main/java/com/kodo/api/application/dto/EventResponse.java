package com.kodo.api.application.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventResponse(
        UUID eventId,
        String gameId,
        String playerId,
        String type,
        Instant occurredAt,
        Instant receivedAt,
        Map<String, Object> payload
) {
}