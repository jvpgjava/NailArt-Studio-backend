package com.nailart.web.mapper;

import com.nailart.infrastructure.persistence.entity.*;
import com.nailart.web.dto.*;

import java.util.List;

public final class DtoMapper {

    public static ServiceDto toServiceDto(ServiceEntity e) {
        List<ServiceOptionDto> options = e.getOptions() == null ? List.of() : e.getOptions().stream()
                .filter(o -> Boolean.TRUE.equals(o.getActive()))
                .map(DtoMapper::toServiceOptionDto)
                .toList();
        return ServiceDto.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .priceCents(e.getPriceCents())
                .durationMin(e.getDurationMin())
                .durationMax(e.getDurationMax())
                .active(e.getActive())
                .options(options)
                .build();
    }

    public static ServiceOptionDto toServiceOptionDto(ServiceOptionEntity o) {
        return ServiceOptionDto.builder()
                .id(o.getId())
                .name(o.getName())
                .priceDeltaCents(o.getPriceDeltaCents())
                .durationDeltaMin(o.getDurationDeltaMin())
                .active(o.getActive())
                .build();
    }

    public static EmployeeDto toEmployeeDto(EmployeeEntity e) {
        List<java.util.UUID> ids = e.getServices() == null ? List.of() : e.getServices().stream()
                .map(ServiceEntity::getId)
                .toList();
        return EmployeeDto.builder()
                .id(e.getId())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .active(e.getActive())
                .serviceIds(ids)
                .build();
    }

    public static AppointmentDto toAppointmentDto(AppointmentEntity a) {
        return AppointmentDto.builder()
                .id(a.getId())
                .clientUserId(a.getClientUser().getId())
                .employeeId(a.getEmployee().getId())
                .serviceId(a.getService().getId())
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .status(a.getStatus())
                .priceCents(a.getPriceCents())
                .durationMin(a.getDurationMin())
                .clientName(a.getClientName())
                .clientEmail(a.getClientEmail())
                .clientPhone(a.getClientPhone())
                .serviceOptionsSnapshot(a.getServiceOptionsSnapshot())
                .cancelReason(a.getCancelReason())
                .employeeName(a.getEmployee().getFullName())
                .serviceName(a.getService().getName())
                .build();
    }

    public static ExpenseDto toExpenseDto(ExpenseEntity e) {
        return ExpenseDto.builder()
                .id(e.getId())
                .category(e.getCategory())
                .amountCents(e.getAmountCents())
                .expenseDate(e.getExpenseDate())
                .description(e.getDescription())
                .build();
    }

    public static UserDto toUserDto(UserEntity u) {
        return UserDto.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .phone(u.getPhone())
                .blocked(u.getBlocked())
                .build();
    }

    public static FinanceDashboardDto toFinanceDashboardDto(com.nailart.application.finance.FinanceService.FinanceDashboard d) {
        return FinanceDashboardDto.builder()
                .receitaEstimada(d.receitaEstimada())
                .perdasCancelamentos(d.perdasCancelamentos())
                .perdasNoShow(d.perdasNoShow())
                .gastosFixos(d.gastosFixos())
                .gastosVariaveis(d.gastosVariaveis())
                .gastosMateriais(d.gastosMateriais())
                .gastosFuncionarios(d.gastosFuncionarios())
                .outros(d.outros())
                .totalDespesas(d.totalDespesas())
                .lucro(d.lucro())
                .build();
    }
}
