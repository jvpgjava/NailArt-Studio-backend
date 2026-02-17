package com.nailart.web.admin;

import com.nailart.application.finance.FinanceService;
import com.nailart.domain.ExpenseCategory;
import com.nailart.infrastructure.persistence.entity.ExpenseEntity;
import com.nailart.web.dto.ExpenseDto;
import com.nailart.web.dto.FinanceDashboardDto;
import com.nailart.web.mapper.DtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/finance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
@Tag(name = "7. Admin - Financeiro", description = "Dashboard financeiro (receita, perdas, despesas por categoria, lucro) e CRUD de despesas. Valores em centavos. Exige **manager** ou **admin**.")
public class AdminFinanceController {

    private final FinanceService financeService;

    @GetMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Dashboard financeiro",
            description = "Resumo do período: receita estimada (CONFIRMED), perdas (CANCELLED/NO_SHOW), despesas por categoria (FIXED, VARIABLE, MATERIALS, EMPLOYEES, OTHER), lucro. Tudo em centavos."
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Dashboard do período") })
    public FinanceDashboardDto getDashboard(
            @Parameter(description = "Data inicial (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Data final (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        var d = financeService.getDashboard(from, to);
        return DtoMapper.toFinanceDashboardDto(d);
    }

    @GetMapping(value = "/expenses", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Listar despesas no período")
    public List<ExpenseDto> listExpenses(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return financeService.listExpenses(from, to).stream().map(DtoMapper::toExpenseDto).toList();
    }

    @PostMapping(value = "/expenses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar despesa")
    public ExpenseDto createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        validateCategory(request.getCategory());
        ExpenseEntity e = financeService.createExpense(
                request.getCategory(),
                request.getAmountCents(),
                request.getExpenseDate(),
                request.getDescription()
        );
        return DtoMapper.toExpenseDto(e);
    }

    @PutMapping(value = "/expenses/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualizar despesa")
    public ExpenseDto updateExpense(@PathVariable UUID id, @RequestBody UpdateExpenseRequest request) {
        if (request.getCategory() != null) validateCategory(request.getCategory());
        ExpenseEntity e = financeService.updateExpense(
                id,
                request.getCategory(),
                request.getAmountCents(),
                request.getExpenseDate(),
                request.getDescription()
        );
        return DtoMapper.toExpenseDto(e);
    }

    @DeleteMapping("/expenses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir despesa")
    public void deleteExpense(@PathVariable UUID id) {
        financeService.deleteExpense(id);
    }

    private static void validateCategory(String category) {
        try {
            ExpenseCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Categoria inválida. Use: FIXED, VARIABLE, MATERIALS, EMPLOYEES, OTHER");
        }
    }

    @lombok.Data
    public static class CreateExpenseRequest {
        @NotBlank
        private String category;
        @Min(0)
        private int amountCents;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate expenseDate;
        private String description;
    }

    @lombok.Data
    public static class UpdateExpenseRequest {
        private String category;
        private Integer amountCents;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate expenseDate;
        private String description;
    }
}
