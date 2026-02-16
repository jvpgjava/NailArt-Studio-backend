package com.nailart.infrastructure.persistence.repository;

import com.nailart.infrastructure.persistence.entity.ServiceOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceOptionJpaRepository extends JpaRepository<ServiceOptionEntity, UUID> {

    List<ServiceOptionEntity> findByServiceIdAndActiveTrue(UUID serviceId);
}
