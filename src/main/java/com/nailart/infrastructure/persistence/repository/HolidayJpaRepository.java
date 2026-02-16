package com.nailart.infrastructure.persistence.repository;

import com.nailart.infrastructure.persistence.entity.HolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface HolidayJpaRepository extends JpaRepository<HolidayEntity, UUID> {

    Optional<HolidayEntity> findByHolidayDate(LocalDate date);
}
