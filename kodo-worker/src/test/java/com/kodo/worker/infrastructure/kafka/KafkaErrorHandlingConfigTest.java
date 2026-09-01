package com.kodo.worker.infrastructure.kafka;

import com.kodo.worker.application.exceptions.InvalidEventException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KafkaErrorHandlingConfigTest {

    @Test
    void shouldRecoverInvalidEventImmediatelyWithoutRetrying() {
        AtomicInteger recoveryCount = new AtomicInteger();

        ConsumerRecordRecoverer recoverer =
                (record, exception) -> recoveryCount.incrementAndGet();

        DefaultErrorHandler errorHandler =
                KafkaErrorHandlingConfig.createErrorHandler(
                        recoverer,
                        new FixedBackOff(0L, 3L)
                );

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>(
                        "telemetry.events",
                        0,
                        0L,
                        null,
                        "invalid-event"
                );

        Consumer<?, ?> consumer = mock(Consumer.class);
        MessageListenerContainer container =
                mock(MessageListenerContainer.class);

        boolean recovered = errorHandler.handleOne(
                new InvalidEventException("gameId is required"),
                record,
                consumer,
                container
        );

        assertThat(recovered).isTrue();
        assertThat(recoveryCount.get()).isEqualTo(1);
    }

    @Test
    void shouldRetryTransientErrorBeforeRecovering() {
        AtomicInteger recoveryCount = new AtomicInteger();

        ConsumerRecordRecoverer recoverer =
                (record, exception) -> recoveryCount.incrementAndGet();

        DefaultErrorHandler errorHandler =
                KafkaErrorHandlingConfig.createErrorHandler(
                        recoverer,
                        new FixedBackOff(0L, 3L)
                );

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>(
                        "telemetry.events",
                        0,
                        0L,
                        null,
                        "valid-event"
                );

        Consumer<?, ?> consumer = mock(Consumer.class);
        MessageListenerContainer container =
                mock(MessageListenerContainer.class);

        RuntimeException transientError =
                new RuntimeException("temporary failure");

        assertThat(errorHandler.handleOne(
                transientError,
                record,
                consumer,
                container
        )).isFalse();

        assertThat(errorHandler.handleOne(
                transientError,
                record,
                consumer,
                container
        )).isFalse();

        assertThat(errorHandler.handleOne(
                transientError,
                record,
                consumer,
                container
        )).isFalse();

        assertThat(recoveryCount.get()).isZero();

        boolean recovered = errorHandler.handleOne(
                transientError,
                record,
                consumer,
                container
        );

        assertThat(recovered).isTrue();
        assertThat(recoveryCount.get()).isEqualTo(1);
    }
}