package com.kodo.worker.application.validation;

import com.kodo.contracts.events.GameEvent;
import com.kodo.worker.application.exceptions.InvalidEventException;
import org.springframework.stereotype.Component;

@Component
public class GameEventValidator {

    public void validate(GameEvent event) {

        if (event.eventId() == null) {
            throw new InvalidEventException("eventId is required");
        }

        if (event.gameId() == null || event.gameId().isBlank()) {
            throw new InvalidEventException("gameId is required");
        }

        if (event.type() == null || event.type().isBlank()) {
            throw new InvalidEventException("type is required");
        }

        if (event.occurredAt() == null) {
            throw new InvalidEventException("occurredAt is required");
        }
    }
}
