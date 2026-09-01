package com.kodo.worker.infrastructure.kafka;

import com.kodo.contracts.events.GameEvent;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<?,?> kafkaProducerFactory(KafkaProperties kafkaProperties) {

        Map<Class<?>, Serializer<?>> serializers = new LinkedHashMap<>();

        serializers.put(
                byte[].class,
                new ByteArraySerializer()
        );

        serializers.put(
                GameEvent.class,
                new JacksonJsonSerializer<>()
        );

        DelegatingByTypeSerializer valueSerializer = new DelegatingByTypeSerializer(serializers);

        return new DefaultKafkaProducerFactory<String, Object>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                valueSerializer
        );
    }
}
