package com.nailart.web.employee;

import com.nailart.application.employees.EmployeeService;
import com.nailart.application.scheduling.SchedulingService;
import com.nailart.infrastructure.persistence.entity.AppointmentEntity;
import com.nailart.infrastructure.persistence.entity.EmployeeEntity;
import com.nailart.web.dto.AppointmentDto;
import com.nailart.web.mapper.DtoMapper;
import com.nailart.web.support.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
@Tag(name = "3. Funcionário", description = "Área do funcionário. Exige role **employee**. Ver dados do funcionário logado e sua agenda do dia.")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SchedulingService schedulingService;

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Meus dados", description = "Retorna os dados do funcionário associado ao usuário autenticado (keycloak_id).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do funcionário"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado para este usuário")
    })
    public com.nailart.web.dto.EmployeeDto getMe() {
        String keycloakId = CurrentUser.getKeycloakIdOrThrow();
        EmployeeEntity e = employeeService.listAll().stream()
                .filter(emp -> keycloakId.equals(emp.getKeycloakId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Funcionário não encontrado para este usuário."));
        return DtoMapper.toEmployeeDto(e);
    }

    @GetMapping(value = "/agenda", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Minha agenda", description = "Lista os agendamentos do funcionário logado em uma data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de agendamentos do dia"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public List<AppointmentDto> getMyAgenda(
            @Parameter(description = "Data (YYYY-MM-DD)", required = true, example = "2025-02-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String keycloakId = CurrentUser.getKeycloakIdOrThrow();
        EmployeeEntity e = employeeService.listAll().stream()
                .filter(emp -> keycloakId.equals(emp.getKeycloakId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Funcionário não encontrado."));
        List<AppointmentEntity> list = schedulingService.getAgendaForDay(date, e.getId());
        return list.stream().map(DtoMapper::toAppointmentDto).toList();
    }
}
