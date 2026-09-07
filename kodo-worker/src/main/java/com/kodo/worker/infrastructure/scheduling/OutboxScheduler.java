package com.kodo.worker.infrastructure.scheduling;

import com.kodo.worker.application.ports.in.OutboxProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "kodo.outbox",
        name = "enabled",
        havingValue = "true"
)
public class OutboxScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxProcessor outboxProcessor;
    private final int batchSize;

    public OutboxScheduler(
            OutboxProcessor outboxProcessor,
            @Value("${kodo.outbox.batch-size}") int batchSize
    ) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "kodo.outbox.batch-size must be greater than zero"
            );
        }

        this.outboxProcessor = outboxProcessor;
        this.batchSize = batchSize;
    }

    @Scheduled(
            fixedDelayString = "${kodo.outbox.poll-interval-ms}"
    )
    public void publishPending() {
        try {
            int publishedCount =
                    outboxProcessor.publishPending(batchSize);

            if (publishedCount > 0) {
                log.debug(
                        "Published {} outbox events",
                        publishedCount
                );
            }
        } catch (RuntimeException e) {
            log.error(
                    "Outbox publishing cycle failed; "
                            + "pending events will be retried on a later cycle",
                    e
            );
        }
    }
}