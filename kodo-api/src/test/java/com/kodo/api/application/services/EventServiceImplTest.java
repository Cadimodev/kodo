package com.kodo.api.application.services;

import com.kodo.api.application.dto.RateLimitResult;
import com.kodo.api.application.exceptions.RateLimitExceededException;
import com.kodo.api.application.ports.out.EventPublisher;
import com.kodo.api.application.ports.out.RateLimiter;
import com.kodo.api.application.dto.requests.CreateEventRequest;
import com.kodo.contracts.events.GameEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private CreateEventRequest request;

    @InjectMocks
    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        when(request.gameId()).thenReturn("test-game");
    }

    @Test
    void shouldPublishEventWhenRateLimitAllowsRequest() {
        when(rateLimiter.tryConsume("test-game", 1))
                .thenReturn(new RateLimitResult(
                        true,
                        9,
                        0
                ));

        when(eventPublisher.publish(any(GameEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(
                () -> eventService.createEvent(request).join()
        );

        verify(rateLimiter).tryConsume("test-game", 1);
        verify(eventPublisher).publish(any(GameEvent.class));
    }

    @Test
    void shouldNotPublishEventWhenRateLimitIsExceeded() {
        when(rateLimiter.tryConsume("test-game", 1))
                .thenReturn(new RateLimitResult(
                        false,
                        0,
                        2
                ));

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> eventService.createEvent(request)
        );

        assertEquals(2, exception.getRetryAfterSeconds());

        verify(rateLimiter).tryConsume("test-game", 1);
        verify(eventPublisher, never()).publish(any(GameEvent.class));
    }
}