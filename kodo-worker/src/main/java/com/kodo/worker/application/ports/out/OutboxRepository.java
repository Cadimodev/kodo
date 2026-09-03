package com.kodo.worker.application.ports.out;

import com.kodo.contracts.events.GameEvent;

public interface OutboxRepository {

    void savePending(GameEvent event);
}