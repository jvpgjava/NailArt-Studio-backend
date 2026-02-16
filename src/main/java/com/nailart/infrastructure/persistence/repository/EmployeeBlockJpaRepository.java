package com.nailart.infrastructure.persistence.repository;

import com.nailart.infrastructure.persistence.entity.EmployeeBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmployeeBlockJpaRepository extends JpaRepository<EmployeeBlockEntity, UUID> {

    List<EmployeeBlockEntity> findByEmployeeIdAndBlockDate(UUID employeeId, LocalDate blockDate);
}
