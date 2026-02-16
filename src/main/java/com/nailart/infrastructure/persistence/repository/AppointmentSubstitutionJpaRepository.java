package com.nailart.infrastructure.persistence.repository;

import com.nailart.infrastructure.persistence.entity.AppointmentSubstitutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentSubstitutionJpaRepository extends JpaRepository<AppointmentSubstitutionEntity, UUID> {

    List<AppointmentSubstitutionEntity> findByAppointmentIdOrderBySubstitutedAtAsc(UUID appointmentId);
}
