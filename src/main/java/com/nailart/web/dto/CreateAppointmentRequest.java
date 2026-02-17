package com.nailart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Dados para criar um novo agendamento. Cliente deve estar autenticado.")
public class CreateAppointmentRequest {
    @NotNull(message = "employeeId é obrigatório")
    @Schema(description = "ID do funcionário", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID employeeId;
    @NotNull(message = "serviceId é obrigatório")
    @Schema(description = "ID do serviço", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID serviceId;
    @NotNull(message = "appointmentDate é obrigatório")
    @Schema(description = "Data desejada (YYYY-MM-DD)", example = "2025-02-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate appointmentDate;
    @NotNull(message = "startTime é obrigatório")
    @Schema(description = "Horário de início (deve estar na lista de disponibilidade)", example = "10:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime startTime;
    @Schema(description = "IDs das opções adicionais do serviço (opcional)")
    private List<UUID> optionIds;
}
