package com.kodo.worker.infrastructure.postgres.repositories;

import com.kodo.worker.infrastructure.postgres.entities.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaEventRepository extends JpaRepository<EventEntity, UUID> {
}
