package com.nailart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Agendamento de atendimento")
public class AppointmentDto {
    @Schema(description = "Identificador único")
    private UUID id;
    @Schema(description = "ID do usuário cliente")
    private UUID clientUserId;
    @Schema(description = "ID do funcionário")
    private UUID employeeId;
    @Schema(description = "ID do serviço")
    private UUID serviceId;
    @Schema(description = "Data do atendimento (YYYY-MM-DD)", example = "2025-02-15")
    private LocalDate appointmentDate;
    @Schema(description = "Hora de início", example = "10:00")
    private LocalTime startTime;
    @Schema(description = "Hora de término", example = "10:30")
    private LocalTime endTime;
    @Schema(description = "Status: CONFIRMED, CANCELLED, NO_SHOW", example = "CONFIRMED")
    private String status;
    @Schema(description = "Preço em centavos no momento do agendamento")
    private Integer priceCents;
    @Schema(description = "Duração em minutos")
    private Integer durationMin;
    @Schema(description = "Nome do cliente (snapshot)")
    private String clientName;
    @Schema(description = "E-mail do cliente (snapshot)")
    private String clientEmail;
    @Schema(description = "Telefone do cliente (snapshot)")
    private String clientPhone;
    @Schema(description = "Opções escolhidas (snapshot)")
    private Map<String, Object> serviceOptionsSnapshot;
    @Schema(description = "Motivo do cancelamento, se houver")
    private String cancelReason;
    @Schema(description = "Nome do funcionário (para exibição)")
    private String employeeName;
    @Schema(description = "Nome do serviço (para exibição)")
    private String serviceName;
}
