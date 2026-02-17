package com.nailart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Resumo financeiro do período (valores em centavos)")
public class FinanceDashboardDto {
    @Schema(description = "Soma dos agendamentos CONFIRMED no período")
    private long receitaEstimada;
    @Schema(description = "Perdas com cancelamentos (CANCELLED)")
    private long perdasCancelamentos;
    @Schema(description = "Perdas com no-show")
    private long perdasNoShow;
    @Schema(description = "Total de despesas fixas")
    private long gastosFixos;
    @Schema(description = "Total de despesas variáveis")
    private long gastosVariaveis;
    @Schema(description = "Total de gastos com materiais")
    private long gastosMateriais;
    @Schema(description = "Total de gastos com funcionários")
    private long gastosFuncionarios;
    @Schema(description = "Outras despesas")
    private long outros;
    @Schema(description = "Soma de todas as despesas")
    private long totalDespesas;
    @Schema(description = "Lucro: receita - perdas - totalDespesas")
    private long lucro;
}
