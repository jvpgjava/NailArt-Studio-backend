package com.nailart.application.scheduling;

import com.nailart.domain.AppointmentStatus;
import com.nailart.infrastructure.persistence.entity.*;
import com.nailart.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchedulingService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final int CANCELLATION_HOURS_BEFORE = 6;

    private final AppointmentJpaRepository appointmentRepo;
    private final AppointmentSubstitutionJpaRepository substitutionRepo;
    private final UserJpaRepository userRepo;
    private final EmployeeJpaRepository employeeRepo;
    private final ServiceJpaRepository serviceRepo;
    private final ServiceOptionJpaRepository serviceOptionRepo;
    private final AvailabilityService availabilityService;

    @Transactional
    public AppointmentEntity createAppointment(
            UUID clientUserId,
            UUID employeeId,
            UUID serviceId,
            LocalDate appointmentDate,
            LocalTime startTime,
            List<UUID> optionIds
    ) {
        UserEntity user = userRepo.findById(clientUserId).orElseThrow(() -> new NoSuchElementException("Cliente não encontrado"));
        if (Boolean.TRUE.equals(user.getBlocked())) {
            throw new IllegalStateException("Cliente bloqueado");
        }
        EmployeeEntity employee = employeeRepo.findById(employeeId).orElseThrow(() -> new NoSuchElementException("Funcionário não encontrado"));
        ServiceEntity service = serviceRepo.findById(serviceId).orElseThrow(() -> new NoSuchElementException("Serviço não encontrado"));
        if (!Boolean.TRUE.equals(employee.getActive())) throw new IllegalStateException("Funcionário inativo");
        if (!Boolean.TRUE.equals(service.getActive())) throw new IllegalStateException("Serviço inativo");
        boolean canPerform = employee.getServices().stream().anyMatch(s -> s.getId().equals(serviceId));
        if (!canPerform) throw new IllegalStateException("Funcionário não atende este serviço");

        int priceCents = service.getPriceCents();
        int durationMin = service.getDurationMin();
        List<ServiceOptionEntity> options = optionIds == null || optionIds.isEmpty()
                ? List.of()
                : serviceOptionRepo.findAllById(optionIds).stream()
                .filter(o -> o.getService().getId().equals(serviceId) && Boolean.TRUE.equals(o.getActive()))
                .toList();
        for (ServiceOptionEntity o : options) {
            priceCents += o.getPriceDeltaCents();
            durationMin += o.getDurationDeltaMin();
        }
        LocalTime endTime = startTime.plusMinutes(durationMin);

        List<LocalTime> available = availabilityService.getAvailableSlots(employeeId, serviceId, appointmentDate);
        if (!available.contains(startTime)) {
            throw new IllegalStateException("Horário indisponível");
        }

        List<AppointmentEntity> conflicts = appointmentRepo.findConfirmedByEmployeeAndDate(employeeId, appointmentDate);
        for (AppointmentEntity a : conflicts) {
            if (timesOverlap(startTime, endTime, a.getStartTime(), a.getEndTime())) {
                throw new IllegalStateException("Conflito de horário");
            }
        }

        Map<String, Object> optionsSnapshot = options.isEmpty() ? null : Map.of(
                "optionIds", options.stream().map(o -> o.getId().toString()).toList(),
                "names", options.stream().map(ServiceOptionEntity::getName).toList()
        );

        AppointmentEntity appointment = AppointmentEntity.builder()
                .clientUser(user)
                .employee(employee)
                .service(service)
                .appointmentDate(appointmentDate)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.CONFIRMED.name())
                .priceCents(priceCents)
                .durationMin(durationMin)
                .clientName(user.getFullName())
                .clientEmail(user.getEmail())
                .clientPhone(user.getPhone())
                .serviceOptionsSnapshot(optionsSnapshot)
                .build();
        try {
            return appointmentRepo.save(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Horário já foi reservado por outra requisição");
        }
    }

    @Transactional
    public void cancelByClient(UUID appointmentId, UUID clientUserId) {
        AppointmentEntity a = appointmentRepo.findById(appointmentId).orElseThrow(() -> new NoSuchElementException("Agendamento não encontrado"));
        if (!a.getClientUser().getId().equals(clientUserId)) {
            throw new SecurityException("Agendamento não pertence ao cliente");
        }
        if (!AppointmentStatus.CONFIRMED.name().equals(a.getStatus())) {
            throw new IllegalStateException("Agendamento já cancelado ou finalizado");
        }
        ZonedDateTime appointmentStart = ZonedDateTime.of(a.getAppointmentDate(), a.getStartTime(), ZONE);
        if (ZonedDateTime.now(ZONE).plusHours(CANCELLATION_HOURS_BEFORE).isAfter(appointmentStart)) {
            throw new IllegalStateException("Cancelamento permitido apenas até 6 horas antes");
        }
        a.setStatus(AppointmentStatus.CANCELLED.name());
        a.setCancelledAt(Instant.now());
        a.setCancelReason("BY_CLIENT");
        appointmentRepo.save(a);
    }

    @Transactional
    public AppointmentEntity substituteEmployee(UUID appointmentId, UUID newEmployeeId, String substitutedBy) {
        AppointmentEntity a = appointmentRepo.findById(appointmentId).orElseThrow(() -> new NoSuchElementException("Agendamento não encontrado"));
        if (!AppointmentStatus.CONFIRMED.name().equals(a.getStatus())) {
            throw new IllegalStateException("Só é possível substituir agendamento confirmado");
        }
        UUID previousEmployeeId = a.getEmployee().getId();
        if (previousEmployeeId.equals(newEmployeeId)) {
            throw new IllegalStateException("Novo funcionário deve ser diferente");
        }
        EmployeeEntity newEmployee = employeeRepo.findById(newEmployeeId).orElseThrow(() -> new NoSuchElementException("Funcionário não encontrado"));
        if (!Boolean.TRUE.equals(newEmployee.getActive())) throw new IllegalStateException("Funcionário inativo");
        boolean canPerform = newEmployee.getServices().stream().anyMatch(s -> s.getId().equals(a.getService().getId()));
        if (!canPerform) throw new IllegalStateException("Funcionário não atende este serviço");

        List<AppointmentEntity> newEmployeeConflicts = appointmentRepo.findConfirmedByEmployeeAndDate(newEmployeeId, a.getAppointmentDate());
        for (AppointmentEntity other : newEmployeeConflicts) {
            if (timesOverlap(a.getStartTime(), a.getEndTime(), other.getStartTime(), other.getEndTime())) {
                throw new IllegalStateException("Novo funcionário tem conflito no horário");
            }
        }

        EmployeeEntity previousEmployee = a.getEmployee();
        a.setEmployee(newEmployee);
        appointmentRepo.save(a);

        AppointmentSubstitutionEntity sub = AppointmentSubstitutionEntity.builder()
                .appointment(a)
                .previousEmployee(previousEmployee)
                .newEmployee(newEmployee)
                .substitutedAt(Instant.now())
                .substitutedBy(substitutedBy)
                .build();
        substitutionRepo.save(sub);
        return a;
    }

    @Transactional(readOnly = true)
    public List<AppointmentEntity> getAppointmentsByClient(UUID clientUserId, LocalDate from, LocalDate to) {
        return appointmentRepo.findByClientUserIdAndAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(clientUserId, from, to);
    }

    @Transactional(readOnly = true)
    public List<AppointmentEntity> getAppointmentsByEmployeeForDay(UUID employeeId, LocalDate date) {
        return appointmentRepo.findByEmployeeIdAndAppointmentDateAndStatus(employeeId, date, AppointmentStatus.CONFIRMED.name());
    }

    @Transactional(readOnly = true)
    public List<AppointmentEntity> getAgendaForDay(LocalDate date, UUID employeeIdOrNull) {
        if (employeeIdOrNull != null) {
            return appointmentRepo.findByEmployeeIdAndAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(
                    employeeIdOrNull, date, date);
        }
        return appointmentRepo.findByAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(date, date);
    }

    private static boolean timesOverlap(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        return s1.compareTo(e2) < 0 && s2.compareTo(e1) < 0;
    }
}
