package com.nailart.infrastructure.persistence.repository;

import com.nailart.infrastructure.persistence.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeJpaRepository extends JpaRepository<EmployeeEntity, UUID> {

    Optional<EmployeeEntity> findByKeycloakId(String keycloakId);

    List<EmployeeEntity> findByActiveTrue();

    @Query("SELECT e FROM EmployeeEntity e JOIN e.services s WHERE s.id = :serviceId AND e.active = true")
    List<EmployeeEntity> findActiveByServiceId(UUID serviceId);
}
