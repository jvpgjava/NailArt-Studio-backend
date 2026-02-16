package com.nailart.application.finance;

import com.nailart.domain.AppointmentStatus;
import com.nailart.domain.ExpenseCategory;
import com.nailart.infrastructure.persistence.entity.AppointmentEntity;
import com.nailart.infrastructure.persistence.entity.ExpenseEntity;
import com.nailart.infrastructure.persistence.repository.AppointmentJpaRepository;
import com.nailart.infrastructure.persistence.repository.ExpenseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final AppointmentJpaRepository appointmentRepo;
    private final ExpenseJpaRepository expenseRepo;

    @Transactional(readOnly = true)
    public FinanceDashboard getDashboard(LocalDate start, LocalDate end) {
        List<AppointmentEntity> confirmed = appointmentRepo
                .findByAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(start, end)
                .stream()
                .filter(a -> AppointmentStatus.CONFIRMED.name().equals(a.getStatus()))
                .toList();
        long receitaEstimada = confirmed.stream().mapToLong(AppointmentEntity::getPriceCents).sum();

        List<AppointmentEntity> allInPeriod = appointmentRepo
                .findByAppointmentDateBetweenOrderByAppointmentDateAscStartTimeAsc(start, end);
        long perdasCancelamentos = allInPeriod.stream()
                .filter(a -> AppointmentStatus.CANCELLED.name().equals(a.getStatus()))
                .mapToLong(AppointmentEntity::getPriceCents)
                .sum();
        long perdasNoShow = allInPeriod.stream()
                .filter(a -> AppointmentStatus.NO_SHOW.name().equals(a.getStatus()))
                .mapToLong(AppointmentEntity::getPriceCents)
                .sum();

        List<ExpenseEntity> expenses = expenseRepo.findByExpenseDateBetweenOrderByExpenseDateAsc(start, end);
        long gastosFixos = sumByCategory(expenses, ExpenseCategory.FIXED);
        long gastosVariaveis = sumByCategory(expenses, ExpenseCategory.VARIABLE);
        long gastosMateriais = sumByCategory(expenses, ExpenseCategory.MATERIALS);
        long gastosFuncionarios = sumByCategory(expenses, ExpenseCategory.EMPLOYEES);
        long outros = sumByCategory(expenses, ExpenseCategory.OTHER);
        long totalDespesas = gastosFixos + gastosVariaveis + gastosMateriais + gastosFuncionarios + outros;

        long lucro = receitaEstimada - perdasCancelamentos - perdasNoShow - totalDespesas;

        return new FinanceDashboard(
                receitaEstimada,
                perdasCancelamentos,
                perdasNoShow,
                gastosFixos,
                gastosVariaveis,
                gastosMateriais,
                gastosFuncionarios,
                outros,
                totalDespesas,
                lucro
        );
    }

    @Transactional(readOnly = true)
    public List<ExpenseEntity> listExpenses(LocalDate start, LocalDate end) {
        return expenseRepo.findByExpenseDateBetweenOrderByExpenseDateAsc(start, end);
    }

    @Transactional
    public ExpenseEntity createExpense(String category, int amountCents, LocalDate expenseDate, String description) {
        ExpenseEntity e = ExpenseEntity.builder()
                .category(category)
                .amountCents(amountCents)
                .expenseDate(expenseDate)
                .description(description)
                .build();
        return expenseRepo.save(e);
    }

    @Transactional
    public ExpenseEntity updateExpense(UUID id, String category, Integer amountCents,
                                       LocalDate expenseDate, String description) {
        ExpenseEntity e = expenseRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Despesa não encontrada"));
        if (category != null) e.setCategory(category);
        if (amountCents != null) e.setAmountCents(amountCents);
        if (expenseDate != null) e.setExpenseDate(expenseDate);
        if (description != null) e.setDescription(description);
        return expenseRepo.save(e);
    }

    @Transactional
    public void deleteExpense(UUID id) {
        expenseRepo.deleteById(id);
    }

    private static long sumByCategory(List<ExpenseEntity> expenses, ExpenseCategory cat) {
        return expenses.stream()
                .filter(e -> cat.name().equals(e.getCategory()))
                .mapToLong(ExpenseEntity::getAmountCents)
                .sum();
    }

    public record FinanceDashboard(
            long receitaEstimada,
            long perdasCancelamentos,
            long perdasNoShow,
            long gastosFixos,
            long gastosVariaveis,
            long gastosMateriais,
            long gastosFuncionarios,
            long outros,
            long totalDespesas,
            long lucro
    ) {}
}
