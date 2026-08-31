package com.kodo.worker.application.ports.in;

import com.kodo.contracts.events.GameEvent;

public interface EventProcessor {

    void process(GameEvent event);
}
