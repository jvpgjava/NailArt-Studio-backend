package com.nailart.infrastructure.persistence.repository;

import com.nailart.infrastructure.persistence.entity.AppointmentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, UUID> {

    List<AppointmentEntity> findByEmployeeIdAndAppointmentDateAndStatus(
            UUID employeeId, LocalDate appointmentDate, String status);

    List<AppointmentEntity> findByClientUserIdAndAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(
            UUID clientUserId, LocalDate start, LocalDate end);

    List<AppointmentEntity> findByEmployeeIdAndAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(
            UUID employeeId, LocalDate start, LocalDate end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AppointmentEntity a WHERE a.employee.id = :employeeId AND a.appointmentDate = :date AND a.status = 'CONFIRMED'")
    List<AppointmentEntity> findConfirmedByEmployeeAndDate(UUID employeeId, LocalDate date);

    List<AppointmentEntity> findByAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(
            LocalDate start, LocalDate end);
}
