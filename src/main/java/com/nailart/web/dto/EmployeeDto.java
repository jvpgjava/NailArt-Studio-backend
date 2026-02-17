package com.nailart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Funcionário do estúdio")
public class EmployeeDto {
    @Schema(description = "Identificador único")
    private UUID id;
    @Schema(description = "Nome completo", example = "Maria Silva")
    private String fullName;
    @Schema(description = "E-mail")
    private String email;
    @Schema(description = "Telefone")
    private String phone;
    @Schema(description = "Se está ativo e pode receber agendamentos", example = "true")
    private Boolean active;
    @Schema(description = "IDs dos serviços que o funcionário atende")
    private List<UUID> serviceIds;
}
