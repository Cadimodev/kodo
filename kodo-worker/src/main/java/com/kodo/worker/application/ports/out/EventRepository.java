package com.kodo.worker.application.ports.out;

import com.kodo.contracts.events.GameEvent;

public interface EventRepository {

    void save(GameEvent event);
}
