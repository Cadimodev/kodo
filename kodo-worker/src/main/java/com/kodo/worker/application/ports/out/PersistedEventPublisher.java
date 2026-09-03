package com.kodo.worker.application.ports.out;

import com.kodo.contracts.events.GameEventPersisted;

public interface PersistedEventPublisher {

    void publish(GameEventPersisted event);
}