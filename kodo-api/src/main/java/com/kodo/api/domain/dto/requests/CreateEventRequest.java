package com.kodo.api.domain.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CreateEventRequest (
        @NotNull(message = "eventId is mandatory")
        UUID eventId,

        @NotBlank(message = "gameId is mandatory")
        String gameId,

        String playerId,

        @NotBlank(message = "type is mandatory")
        String type,

        @NotNull(message = "occurredAt is mandatory")
        Instant occurredAt,

        Map<String, Object> payload
)
{
}
