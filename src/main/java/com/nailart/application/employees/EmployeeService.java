package com.nailart.application.employees;

import com.nailart.infrastructure.persistence.entity.EmployeeAvailabilityEntity;
import com.nailart.infrastructure.persistence.entity.EmployeeBlockEntity;
import com.nailart.infrastructure.persistence.entity.EmployeeEntity;
import com.nailart.infrastructure.persistence.entity.ServiceEntity;
import com.nailart.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeJpaRepository employeeRepo;
    private final ServiceJpaRepository serviceRepo;
    private final EmployeeAvailabilityJpaRepository availabilityRepo;
    private final EmployeeBlockJpaRepository blockRepo;

    @Transactional(readOnly = true)
    public List<EmployeeEntity> listActive() {
        return employeeRepo.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<EmployeeEntity> listByServiceId(UUID serviceId) {
        return employeeRepo.findActiveByServiceId(serviceId);
    }

    @Transactional(readOnly = true)
    public List<EmployeeEntity> listAll() {
        return employeeRepo.findAll();
    }

    @Transactional(readOnly = true)
    public EmployeeEntity getById(UUID id) {
        return employeeRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Funcionário não encontrado"));
    }

    @Transactional
    public EmployeeEntity create(String fullName, String email, String phone, List<UUID> serviceIds) {
        EmployeeEntity e = EmployeeEntity.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .active(true)
                .build();
        e = employeeRepo.save(e);
        if (serviceIds != null && !serviceIds.isEmpty()) {
            Set<ServiceEntity> services = serviceIds.stream()
                    .map(serviceRepo::findById)
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toSet());
            e.setServices(services);
            e = employeeRepo.save(e);
        }
        return e;
    }

    @Transactional
    public EmployeeEntity update(UUID id, String fullName, String email, String phone,
                                 Boolean active, List<UUID> serviceIds) {
        EmployeeEntity e = getById(id);
        if (fullName != null) e.setFullName(fullName);
        if (email != null) e.setEmail(email);
        if (phone != null) e.setPhone(phone);
        if (active != null) e.setActive(active);
        if (serviceIds != null) {
            Set<ServiceEntity> services = serviceIds.stream()
                    .map(serviceRepo::findById)
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toSet());
            e.setServices(services);
        }
        return employeeRepo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        employeeRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EmployeeAvailabilityEntity> getAvailability(UUID employeeId) {
        return availabilityRepo.findByEmployeeIdOrderByDayOfWeekAscStartTimeAsc(employeeId);
    }

    @Transactional
    public EmployeeAvailabilityEntity addAvailability(UUID employeeId, int dayOfWeek,
                                                      LocalTime startTime, LocalTime endTime, boolean isLunchBreak) {
        EmployeeEntity employee = getById(employeeId);
        EmployeeAvailabilityEntity a = EmployeeAvailabilityEntity.builder()
                .employee(employee)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .isLunchBreak(isLunchBreak)
                .build();
        return availabilityRepo.save(a);
    }

    @Transactional(readOnly = true)
    public List<EmployeeBlockEntity> getBlocks(UUID employeeId, LocalDate date) {
        return blockRepo.findByEmployeeIdAndBlockDate(employeeId, date);
    }

    @Transactional
    public EmployeeBlockEntity addBlock(UUID employeeId, LocalDate blockDate,
                                        LocalTime startTime, LocalTime endTime, String reason) {
        EmployeeEntity employee = getById(employeeId);
        EmployeeBlockEntity b = EmployeeBlockEntity.builder()
                .employee(employee)
                .blockDate(blockDate)
                .startTime(startTime)
                .endTime(endTime)
                .reason(reason)
                .build();
        return blockRepo.save(b);
    }
}
