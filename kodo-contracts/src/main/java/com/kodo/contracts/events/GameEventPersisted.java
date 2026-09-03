package com.kodo.contracts.events;

import java.util.UUID;

public record GameEventPersisted(
        UUID eventId,
        String gameId,
        String eventType
) {
}