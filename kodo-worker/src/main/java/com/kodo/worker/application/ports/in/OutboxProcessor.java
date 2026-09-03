package com.kodo.worker.application.ports.in;

public interface OutboxProcessor {

    int publishPending(int batchSize);
}