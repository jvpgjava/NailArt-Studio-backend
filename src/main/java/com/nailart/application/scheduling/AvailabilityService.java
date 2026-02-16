package com.nailart.application.scheduling;

import com.nailart.infrastructure.persistence.entity.*;
import com.nailart.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private static final int DEFAULT_SLOT_MINUTES = 15;
    private static final int DEFAULT_BUFFER_MINUTES = 10;

    private final EmployeeJpaRepository employeeRepo;
    private final ServiceJpaRepository serviceRepo;
    private final EmployeeAvailabilityJpaRepository availabilityRepo;
    private final EmployeeBlockJpaRepository blockRepo;
    private final HolidayJpaRepository holidayRepo;
    private final AppointmentJpaRepository appointmentRepo;
    private final StudioSettingsJpaRepository settingsRepo;

    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(UUID employeeId, UUID serviceId, LocalDate date) {
        var employee = employeeRepo.findById(employeeId).orElseThrow(() -> new NoSuchElementException("Funcionário não encontrado"));
        var service = serviceRepo.findById(serviceId).orElseThrow(() -> new NoSuchElementException("Serviço não encontrado"));
        if (!Boolean.TRUE.equals(employee.getActive()) || !Boolean.TRUE.equals(service.getActive())) {
            return List.of();
        }
        if (!employee.getServices().stream().anyMatch(s -> s.getId().equals(serviceId))) {
            return List.of();
        }

        // DB: 1=domingo, 7=sábado. Java DayOfWeek: MONDAY=1, SUNDAY=7 -> map to 2=seg..7=dom->1
        int javaDay = date.getDayOfWeek().getValue();
        int dayOfWeek = javaDay == 7 ? 1 : javaDay + 1;
        var settingsOpt = settingsRepo.findFirstByOrderByCreatedAtAsc();
        int bufferMinutes = settingsOpt
                .map(StudioSettingsEntity::getBufferMinutes)
                .orElse(DEFAULT_BUFFER_MINUTES);
        int slotMinutes = settingsOpt
                .map(StudioSettingsEntity::getSlotMinutes)
                .orElse(DEFAULT_SLOT_MINUTES);
        int blockMinutes = service.getDurationMax() + bufferMinutes;
        int durationMin = service.getDurationMin();

        if (holidayRepo.findByHolidayDate(date).isPresent()) {
            return List.of();
        }

        List<TimeWindow> windows = getWindowsForDay(employeeId, date, dayOfWeek);
        if (windows.isEmpty()) return List.of();

        List<OccupiedSlot> occupied = getOccupiedSlots(employeeId, date);

        List<LocalTime> slots = new ArrayList<>();
        for (TimeWindow w : windows) {
            LocalTime slotStart = roundToSlot(w.start, slotMinutes);
            while (slotStart.plusMinutes(blockMinutes).compareTo(w.end) <= 0) {
                LocalTime slotEnd = slotStart.plusMinutes(durationMin);
                if (!slotEnd.isAfter(w.end) && !overlapsAny(slotStart, slotEnd, occupied)) {
                    slots.add(slotStart);
                }
                slotStart = slotStart.plusMinutes(slotMinutes);
            }
        }
        return slots.stream().distinct().sorted().toList();
    }

    private List<TimeWindow> getWindowsForDay(UUID employeeId, LocalDate date, int dayOfWeek) {
        List<EmployeeAvailabilityEntity> avail = availabilityRepo.findByEmployeeIdOrderByDayOfWeekAscStartTimeAsc(employeeId);
        List<TimeWindow> fromAvailability = avail.stream()
                .filter(a -> a.getDayOfWeek().equals(dayOfWeek))
                .map(a -> new TimeWindow(a.getStartTime(), a.getEndTime()))
                .collect(Collectors.toList());
        List<EmployeeBlockEntity> blocks = blockRepo.findByEmployeeIdAndBlockDate(employeeId, date);
        if (blocks.isEmpty()) return fromAvailability;
        return subtractBlocks(fromAvailability, blocks);
    }

    private List<TimeWindow> subtractBlocks(List<TimeWindow> windows, List<EmployeeBlockEntity> blocks) {
        List<TimeWindow> result = new ArrayList<>(windows);
        for (EmployeeBlockEntity b : blocks) {
            List<TimeWindow> next = new ArrayList<>();
            for (TimeWindow w : result) {
                if (b.getEndTime().compareTo(w.start) <= 0 || b.getStartTime().compareTo(w.end) >= 0) {
                    next.add(w);
                } else {
                    if (w.start.compareTo(b.getStartTime()) < 0) {
                        next.add(new TimeWindow(w.start, b.getStartTime()));
                    }
                    if (b.getEndTime().compareTo(w.end) < 0) {
                        next.add(new TimeWindow(b.getEndTime(), w.end));
                    }
                }
            }
            result = next;
        }
        return result;
    }

    private List<OccupiedSlot> getOccupiedSlots(UUID employeeId, LocalDate date) {
        List<AppointmentEntity> confirmed = appointmentRepo.findConfirmedByEmployeeAndDate(employeeId, date);
        List<OccupiedSlot> occupied = confirmed.stream()
                .map(a -> new OccupiedSlot(a.getStartTime(), a.getEndTime()))
                .toList();
        List<EmployeeBlockEntity> blocks = blockRepo.findByEmployeeIdAndBlockDate(employeeId, date);
        for (EmployeeBlockEntity b : blocks) {
            occupied = new ArrayList<>(occupied);
            occupied.add(new OccupiedSlot(b.getStartTime(), b.getEndTime()));
        }
        return occupied;
    }

    private static LocalTime roundToSlot(LocalTime t, int slotMinutes) {
        int min = t.getHour() * 60 + t.getMinute();
        int rounded = (min / slotMinutes) * slotMinutes;
        return LocalTime.of(rounded / 60, rounded % 60);
    }

    private static boolean overlapsAny(LocalTime start, LocalTime end, List<OccupiedSlot> occupied) {
        for (OccupiedSlot o : occupied) {
            if (start.compareTo(o.end) < 0 && o.start.compareTo(end) < 0) return true;
        }
        return false;
    }

    private record TimeWindow(LocalTime start, LocalTime end) {}
    private record OccupiedSlot(LocalTime start, LocalTime end) {}
}
