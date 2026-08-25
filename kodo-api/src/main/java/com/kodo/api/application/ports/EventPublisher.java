package com.kodo.api.application.ports;

import com.kodo.contracts.events.GameEvent;

import java.util.concurrent.CompletableFuture;

public interface EventPublisher {

    CompletableFuture<Void> publish(GameEvent event);
}
