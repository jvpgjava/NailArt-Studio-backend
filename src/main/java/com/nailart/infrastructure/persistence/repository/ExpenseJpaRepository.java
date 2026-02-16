package com.nailart.infrastructure.persistence.repository;

import com.nailart.infrastructure.persistence.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, UUID> {

    List<ExpenseEntity> findByExpenseDateBetweenOrderByExpenseDateAsc(LocalDate start, LocalDate end);
}
