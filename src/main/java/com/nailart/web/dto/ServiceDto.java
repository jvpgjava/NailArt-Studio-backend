package com.nailart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Serviço oferecido pelo estúdio")
public class ServiceDto {
    @Schema(description = "Identificador único", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    @Schema(description = "Nome do serviço", example = "Manicure")
    private String name;
    @Schema(description = "Descrição opcional")
    private String description;
    @Schema(description = "Preço em centavos", example = "5000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer priceCents;
    @Schema(description = "Duração mínima em minutos", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer durationMin;
    @Schema(description = "Duração máxima em minutos", example = "45", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer durationMax;
    @Schema(description = "Se o serviço está ativo e visível", example = "true")
    private Boolean active;
    @Schema(description = "Opções adicionais que alteram preço/duração")
    private List<ServiceOptionDto> options;
}
