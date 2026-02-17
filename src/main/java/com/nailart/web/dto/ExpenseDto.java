package com.nailart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Despesa registrada (controle financeiro)")
public class ExpenseDto {
    @Schema(description = "Identificador único")
    private UUID id;
    @Schema(description = "Categoria: FIXED, VARIABLE, MATERIALS, EMPLOYEES, OTHER", example = "FIXED")
    private String category;
    @Schema(description = "Valor em centavos", example = "150000")
    private Integer amountCents;
    @Schema(description = "Data da despesa (YYYY-MM-DD)", example = "2025-02-10")
    private LocalDate expenseDate;
    @Schema(description = "Descrição ou observação")
    private String description;
}
