package com.nailart.infrastructure.persistence.repository;

import com.nailart.infrastructure.persistence.entity.StudioSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudioSettingsJpaRepository extends JpaRepository<StudioSettingsEntity, UUID> {

    Optional<StudioSettingsEntity> findFirstByOrderByCreatedAtAsc();
}
