package com.nailart.web.admin;

import com.nailart.application.scheduling.SchedulingService;
import com.nailart.infrastructure.persistence.entity.AppointmentEntity;
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
@RequestMapping("/api/admin/agenda")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
@Tag(name = "6. Admin - Agenda", description = "Consulta da agenda por dia (todos ou por funcionário) e substituição de funcionário em atendimento. Exige **manager** ou **admin**.")
public class AdminAgendaController {

    private final SchedulingService schedulingService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Agenda por dia", description = "Lista agendamentos de uma data. Se employeeId for informado, filtra por esse funcionário; senão, retorna todos.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Lista de agendamentos") })
    public List<AppointmentDto> getAgenda(
            @Parameter(description = "Data (YYYY-MM-DD)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "UUID do funcionário (opcional)") @RequestParam(required = false) UUID employeeId
    ) {
        List<AppointmentEntity> list = schedulingService.getAgendaForDay(date, employeeId);
        return list.stream().map(DtoMapper::toAppointmentDto).toList();
    }

    @PostMapping(value = "/appointments/{id}/substitute", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Substituir funcionário",
            description = "Troca o funcionário de um agendamento confirmado. O novo funcionário deve atender o serviço e não ter conflito no horário. Registra histórico da substituição."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento atualizado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado"),
            @ApiResponse(responseCode = "422", description = "Agendamento não confirmado, conflito ou funcionário não atende o serviço")
    })
    public AppointmentDto substituteEmployee(
            @Parameter(description = "UUID do agendamento", required = true) @PathVariable UUID id,
            @RequestBody SubstituteRequest request) {
        String substitutedBy = CurrentUser.getKeycloakIdOrThrow();
        AppointmentEntity a = schedulingService.substituteEmployee(id, request.getNewEmployeeId(), substitutedBy);
        return DtoMapper.toAppointmentDto(a);
    }

    @lombok.Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "ID do novo funcionário para o atendimento")
    public static class SubstituteRequest {
        @io.swagger.v3.oas.annotations.media.Schema(description = "UUID do funcionário que assumirá o atendimento", required = true)
        private UUID newEmployeeId;
    }
}
