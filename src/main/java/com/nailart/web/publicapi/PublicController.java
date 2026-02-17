package com.nailart.web.publicapi;

import com.nailart.application.employees.EmployeeService;
import com.nailart.application.scheduling.AvailabilityService;
import com.nailart.application.services.ServiceCatalogService;
import com.nailart.infrastructure.persistence.entity.EmployeeEntity;
import com.nailart.infrastructure.persistence.entity.ServiceEntity;
import com.nailart.web.dto.EmployeeDto;
import com.nailart.web.dto.ServiceDto;
import com.nailart.web.mapper.DtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "1. Público", description = "Endpoints públicos — **não exigem autenticação**. Listagem de serviços, funcionários por serviço e slots disponíveis.")
@SecurityRequirements
public class PublicController {

    private final ServiceCatalogService serviceCatalogService;
    private final EmployeeService employeeService;
    private final AvailabilityService availabilityService;

    @GetMapping(value = "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar serviços ativos", description = "Retorna todos os serviços ativos (visíveis para agendamento). Inclui opções adicionais de cada serviço.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de serviços")
    })
    public List<ServiceDto> listServices() {
        List<ServiceEntity> list = serviceCatalogService.listActive();
        return list.stream().map(DtoMapper::toServiceDto).toList();
    }

    @GetMapping(value = "/employees/by-service/{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar funcionários por serviço", description = "Retorna os funcionários ativos que realizam o serviço informado. Use o serviceId obtido em GET /services.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de funcionários"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public List<EmployeeDto> listEmployeesByService(
            @Parameter(description = "UUID do serviço", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID serviceId) {
        List<EmployeeEntity> list = employeeService.listByServiceId(serviceId);
        return list.stream().map(DtoMapper::toEmployeeDto).toList();
    }

    @GetMapping(value = "/availability", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Slots disponíveis",
            description = "Retorna os horários de início possíveis para um funcionário + serviço em uma data. " +
                    "Respeita slot de 15 min, buffer 10 min, disponibilidade do funcionário, feriados e bloqueios. " +
                    "Formato da resposta: array de horários (HH:mm)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de horários (LocalTime) em que é possível iniciar o atendimento",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = String.class))))
    })
    public List<LocalTime> getAvailability(
            @Parameter(description = "UUID do funcionário", required = true) @RequestParam UUID employeeId,
            @Parameter(description = "UUID do serviço", required = true) @RequestParam UUID serviceId,
            @Parameter(description = "Data (YYYY-MM-DD)", required = true, example = "2025-02-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return availabilityService.getAvailableSlots(employeeId, serviceId, date);
    }
}
