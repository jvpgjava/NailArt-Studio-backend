package com.nailart.infrastructure.persistence.repository;

import com.nailart.infrastructure.persistence.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceJpaRepository extends JpaRepository<ServiceEntity, UUID> {

    @Query("SELECT s FROM ServiceEntity s LEFT JOIN FETCH s.options WHERE s.active = true")
    List<ServiceEntity> findByActiveTrue();

    @Query("SELECT s FROM ServiceEntity s LEFT JOIN FETCH s.options WHERE s.id = :id")
    Optional<ServiceEntity> findByIdWithOptions(UUID id);
}
