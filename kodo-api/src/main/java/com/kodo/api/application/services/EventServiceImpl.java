package com.kodo.api.application.services;

import com.kodo.api.application.dto.RateLimitResult;
import com.kodo.api.application.exceptions.RateLimitExceededException;
import com.kodo.api.application.ports.out.EventPublisher;
import com.kodo.api.application.ports.out.RateLimiter;
import com.kodo.api.application.dto.requests.CreateEventRequest;
import com.kodo.api.application.ports.in.EventService;
import com.kodo.contracts.events.GameEvent;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventServiceImpl implements EventService {

    private final EventPublisher eventPublisher;
    private final RateLimiter rateLimiter;

    public EventServiceImpl(EventPublisher eventPublisher, RateLimiter rateLimiter) {
        this.eventPublisher = eventPublisher;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public CompletableFuture<Void> createEvent(CreateEventRequest request) {

        // TODO: Replace gameId with an authenticated client identity once authentication/API keys are introduced.
        String rateLimitKey = request.gameId();;
        RateLimitResult rateLimitResult = rateLimiter.tryConsume(rateLimitKey, 1);

        if (!rateLimitResult.allowed()) {
            throw new RateLimitExceededException(rateLimitResult.retryAfterSeconds());
        }

        GameEvent event = new GameEvent(
                request.eventId(),
                request.gameId(),
                request.playerId(),
                request.type(),
                request.occurredAt(),
                request.payload()
        );

        return eventPublisher.publish(event);
    }
}
