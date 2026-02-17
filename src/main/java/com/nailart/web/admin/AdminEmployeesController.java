package com.nailart.web.admin;

import com.nailart.application.employees.EmployeeService;
import com.nailart.infrastructure.persistence.entity.EmployeeAvailabilityEntity;
import com.nailart.infrastructure.persistence.entity.EmployeeBlockEntity;
import com.nailart.infrastructure.persistence.entity.EmployeeEntity;
import com.nailart.web.dto.EmployeeDto;
import com.nailart.web.mapper.DtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
@Tag(name = "5. Admin - Funcionários", description = "CRUD de funcionários, disponibilidade semanal e bloqueios (folgas/exceções). Exige **manager** ou **admin**.")
public class AdminEmployeesController {

    private final EmployeeService employeeService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar todos os funcionários")
    public List<EmployeeDto> listAll() {
        return employeeService.listAll().stream().map(DtoMapper::toEmployeeDto).toList();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Buscar funcionário por ID")
    public EmployeeDto getById(@PathVariable UUID id) {
        return DtoMapper.toEmployeeDto(employeeService.getById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar funcionário")
    public EmployeeDto create(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeEntity e = employeeService.create(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getServiceIds()
        );
        return DtoMapper.toEmployeeDto(e);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualizar funcionário")
    public EmployeeDto update(@PathVariable UUID id, @RequestBody UpdateEmployeeRequest request) {
        EmployeeEntity e = employeeService.update(
                id,
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getActive(),
                request.getServiceIds()
        );
        return DtoMapper.toEmployeeDto(e);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir funcionário")
    public void delete(@PathVariable UUID id) {
        employeeService.delete(id);
    }

    @GetMapping(value = "/{id}/availability", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar disponibilidade do funcionário")
    public List<AvailabilitySlotDto> getAvailability(@PathVariable UUID id) {
        return employeeService.getAvailability(id).stream()
                .map(a -> new AvailabilitySlotDto(
                        a.getId(),
                        a.getDayOfWeek(),
                        a.getStartTime(),
                        a.getEndTime(),
                        a.getIsLunchBreak()
                ))
                .toList();
    }

    @PostMapping(value = "/{id}/availability", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar janela de disponibilidade")
    public AvailabilitySlotDto addAvailability(@PathVariable UUID id, @Valid @RequestBody AddAvailabilityRequest request) {
        EmployeeAvailabilityEntity a = employeeService.addAvailability(
                id,
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.isLunchBreak()
        );
        return new AvailabilitySlotDto(a.getId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime(), a.getIsLunchBreak());
    }

    @PostMapping(value = "/{id}/blocks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar bloqueio (folga, exceção)")
    public BlockDto addBlock(@PathVariable UUID id, @Valid @RequestBody AddBlockRequest request) {
        EmployeeBlockEntity b = employeeService.addBlock(
                id,
                request.getBlockDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getReason()
        );
        return new BlockDto(b.getId(), b.getBlockDate(), b.getStartTime(), b.getEndTime(), b.getReason());
    }

    @lombok.Data
    public static class CreateEmployeeRequest {
        @NotBlank
        private String fullName;
        private String email;
        private String phone;
        private List<UUID> serviceIds;
    }

    @lombok.Data
    public static class UpdateEmployeeRequest {
        private String fullName;
        private String email;
        private String phone;
        private Boolean active;
        private List<UUID> serviceIds;
    }

    @lombok.Data
    public static class AddAvailabilityRequest {
        private int dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean lunchBreak;
    }

    @lombok.Data
    public static class AddBlockRequest {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate blockDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String reason;
    }

    public record AvailabilitySlotDto(java.util.UUID id, int dayOfWeek, LocalTime startTime, LocalTime endTime, boolean lunchBreak) {}
    public record BlockDto(java.util.UUID id, LocalDate blockDate, LocalTime startTime, LocalTime endTime, String reason) {}
}
