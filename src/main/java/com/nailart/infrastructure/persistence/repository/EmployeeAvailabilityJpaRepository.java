package com.nailart.infrastructure.persistence.repository;

import com.nailart.infrastructure.persistence.entity.EmployeeAvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeAvailabilityJpaRepository extends JpaRepository<EmployeeAvailabilityEntity, UUID> {

    List<EmployeeAvailabilityEntity> findByEmployeeIdOrderByDayOfWeekAscStartTimeAsc(UUID employeeId);
}
