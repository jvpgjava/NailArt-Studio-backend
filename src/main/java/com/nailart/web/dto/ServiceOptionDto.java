package com.nailart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Opção adicional do serviço (ex.: alongamento, decoração)")
public class ServiceOptionDto {
    @Schema(description = "Identificador único")
    private UUID id;
    @Schema(description = "Nome da opção", example = "Alongamento em gel")
    private String name;
    @Schema(description = "Variação de preço em centavos (pode ser negativo)", example = "1500")
    private Integer priceDeltaCents;
    @Schema(description = "Variação de duração em minutos", example = "30")
    private Integer durationDeltaMin;
    @Schema(description = "Se a opção está ativa")
    private Boolean active;
}